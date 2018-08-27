package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.PollNotFoundError
import io.github.cnguy.strawpoll.domain.answers.{AnswerService, AnswerWithNoPollId}
import io.github.cnguy.strawpoll.domain.polls.{Poll, PollService}

case class PollRequest(question: String, answers: List[AnswerWithNoPollId])

class PollEndpoints[F[_]: Effect] extends Http4sDsl[F] {
  import cats.implicits._

  implicit val pollRequestDecoder = jsonOf[F, PollRequest]
  implicit val pollDecoder = jsonOf[F, Poll]

  def createPollEndpoint(
      pollService: PollService[F],
      answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "polls" => {
        for {
          pollRequest <- req.as[PollRequest]
          saved <- pollService.createPoll(Poll(question = pollRequest.question))
          _ <- answerService.createMultipleAnswersForPoll(saved.id.get, pollRequest.answers)
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

  def endpoints(pollService: PollService[F], answerService: AnswerService[F]): HttpService[F] =
    createPollEndpoint(pollService, answerService) <+> getPollEndpoint(pollService)
}

object PollEndpoints {
  def endpoints[F[_]: Effect](
      pollService: PollService[F],
      answerService: AnswerService[F]): HttpService[F] =
    new PollEndpoints[F].endpoints(pollService, answerService)
}
