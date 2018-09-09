package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import io.github.cnguy.strawpoll.domain.{AnswerNotFoundError, PollNotFoundError}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerService}
import io.github.cnguy.strawpoll.domain.ips.{IpAddressService}
import io.github.cnguy.strawpoll.domain.polls.PollSecurityType.{BrowserCookieCheck, IpAddressCheck}
import io.github.cnguy.strawpoll.domain.polls.PollService

class AnswerEndpoints[F[_]: Effect] extends Http4sDsl[F] {
  import cats.implicits._

  implicit val answerDecoder = jsonOf[F, Answer]
  implicit val answersDecoder = jsonOf[F, List[Answer]]

  private def listAnswersFromPoll(answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "polls" / LongVar(pollId) / "answers" =>
        for {
          retrieved <- answerService.list(pollId)
          resp <- Ok(retrieved.asJson)
        } yield resp
    }

  private def voteAnswerEndpoint(
      answerService: AnswerService[F],
      pollService: PollService[F],
      ipAddressService: IpAddressService[F]): HttpService[F] =
    HttpService[F] {
      // TODO: Clean up this mess!
      case req @ PUT -> Root / "answers" / LongVar(id) =>
        for {
          resp <- answerService.get(id).value.flatMap {
            case Right(found) => {
              for {
                resp <- pollService.get(found.pollId).value.flatMap {
                  case Right(found) => {
                    found.securityType.get match {
                      case IpAddressCheck => {
                        for {
                          ipAddresses <- ipAddressService.list(id)
                          exists = ipAddresses.exists(_.value == req.remoteAddr.getOrElse("null"))
                          resp <- if (exists) {
                            Forbidden("You cannot vote again!")
                          } else {
                            for {
                              answer <- answerService.vote(id)
                              _ = println(req.remoteAddr)
                              innerResp <- answer match {
                                case Some(a) => Ok(a.copy(count = a.count + 1).asJson)
                                case None => NotFound("The answer was not found.")
                              }
                            } yield innerResp
                          }
                        } yield resp
                      }
                      case BrowserCookieCheck => {
                        // TODO
                        for {
                          answer <- answerService.vote(id)
                          resp <- answer match {
                            case Some(a) => Ok(a.copy(count = a.count + 1).asJson)
                            case None => NotFound()
                          }
                        } yield resp
                      }
                      case _ => {
                        for {
                          answer <- answerService.vote(id)
                          resp <- answer match {
                            case Some(a) => Ok(a.copy(count = a.count + 1).asJson)
                            case None => NotFound()
                          }
                        } yield resp
                      }
                    }
                  }
                  case Left(PollNotFoundError) => NotFound("The poll was not found.")
                }
              } yield resp
            }
            case Left(AnswerNotFoundError) => NotFound("The answer was not found.")
          }

          /*
          ipAddresses <- ipAddressService.list(id)
          exists = ipAddresses.exists(_.value == req.remoteAddr.getOrElse("null"))
          _ = println(ipAddresses)
          _ = println(exists)
          resp <- if (exists) {
            NotFound()
          } else {
            for {
              answer <- answerService.vote(id)
              _ = println(req.remoteAddr)
              innerResp <- answer match {
              case Some(a) => Ok (a.copy(count = a.count + 1).asJson)
              case None => NotFound ()
            }
            } yield innerResp
          }*/
        } yield resp
    }

  def endpoints(
      answerService: AnswerService[F],
      pollService: PollService[F],
      ipAddressService: IpAddressService[F]): HttpService[F] =
    listAnswersFromPoll(answerService) <+> voteAnswerEndpoint(
      answerService,
      pollService,
      ipAddressService)
}

object AnswerEndpoints {
  def endpoints[F[_]: Effect](
      answerService: AnswerService[F],
      pollService: PollService[F],
      ipAddressService: IpAddressService[F]): HttpService[F] =
    new AnswerEndpoints[F].endpoints(answerService, pollService, ipAddressService)
}
