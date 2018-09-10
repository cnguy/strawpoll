package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.PollNotFoundError
import io.github.cnguy.strawpoll.domain.answers.{AnswerService}
import io.github.cnguy.strawpoll.domain.polls.{Poll, PollRequest, PollService}

class PollEndpoints[F[_]: Effect] extends Http4sDsl[F] {
  import cats.implicits._

  implicit val pollRequestDecoder = jsonOf[F, PollRequest]
  implicit val pollDecoder = jsonOf[F, Poll]

  private def getPollEndpoint(pollService: PollService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "polls" / LongVar(id) =>
        pollService.get(id).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(PollNotFoundError) => NotFound("The poll was not found.")
        }
    }

  def createPollEndpoint(
      pollService: PollService[F],
      answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "polls" => {
        for {
          pollRequest <- req.as[PollRequest]
          saved <- pollService.create(pollRequest.toPoll)
          _ <- answerService.createBatch(saved.id.get, pollRequest.answers)
          resp <- Ok(saved.asJson)
        } yield resp
      }
    }

  def endpoints(pollService: PollService[F], answerService: AnswerService[F]): HttpService[F] =
    getPollEndpoint(pollService) <+> createPollEndpoint(pollService, answerService)
}

object PollEndpoints {
  def endpoints[F[_]: Effect](
      pollService: PollService[F],
      answerService: AnswerService[F]): HttpService[F] =
    new PollEndpoints[F].endpoints(pollService, answerService)
}
