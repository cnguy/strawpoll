package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.PollNotFoundError
import io.github.cnguy.strawpoll.domain.polls.{Poll, PollService}

class PollEndpoints[F[_]: Effect] extends Http4sDsl[F] {
  import cats.implicits._

  implicit val pollDecoder = jsonOf[F, Poll]

  def createPollEndpoint(pollService: PollService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "polls" => {
        for {
          poll <- req.as[Poll]
          saved <- pollService.createPoll(poll)
          resp <- Ok(saved.asJson)
        } yield resp
      }
    }

  private def getPollEndpoint(pollService: PollService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "polls" / LongVar(id) =>
        pollService.get(id).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(PollNotFoundError) => NotFound("The poll was not found.")
        }
    }

  def endpoints(pollService: PollService[F]): HttpService[F] =
    createPollEndpoint(pollService) <+> getPollEndpoint(pollService)
}

object PollEndpoints {
  def endpoints[F[_]: Effect](pollService: PollService[F]): HttpService[F] =
    new PollEndpoints[F].endpoints(pollService)
}
