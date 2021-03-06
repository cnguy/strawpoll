package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerService}
import io.github.cnguy.strawpoll.domain.ips.{IpAddress, IpAddressService}
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
      case req @ PUT -> Root / "answers" / LongVar(id) =>
        for {
          answer <- answerService.get(id).value
          poll <- pollService.get(answer.right.get.pollId).value
          resp <- poll.right.get.securityType match {
            case Some(IpAddressCheck) => {
              for {
                ipAddresses <- ipAddressService.list(id)
                _ = println("IpAddressCheck")
                exists = ipAddresses.exists(_.value == req.remoteAddr.getOrElse("null"))
                resp <- if (exists) {
                  Forbidden("You cannot vote again!")
                } else {
                  for {
                    answer <- answerService.vote(id)
                    _ <- ipAddressService.create(IpAddress(id, req.remoteAddr.getOrElse("null")))
                    _ = println(req.remoteAddr)
                    innerResp <- Ok(answer.get.vote.asJson)
                  } yield innerResp
                }
              } yield resp
            }
            case Some(BrowserCookieCheck) => {
              // TODO
              for {
                answer <- answerService.vote(id)
                _ = println("BrowserCookieCheck")
                resp <- Ok(answer.get.vote.asJson)
              } yield resp
            }
            case None =>
              for {
                answer <- answerService.vote(id)
                resp <- Ok(answer.get.vote.asJson)
              } yield resp
          }
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
