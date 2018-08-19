package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.AnswerNotFoundError
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerService}

class AnswerEndpoints[F[_]: Effect] extends Http4sDsl[F] {
  import cats.implicits._

  implicit val answerDecoder = jsonOf[F, Answer]

  def createAnswerEndpoint(answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case req @ POST -> Root / "answers" => {
        for {
          answer <- req.as[Answer]
          saved <- answerService.createAnswer(answer)
          resp <- Ok(saved.asJson)
        } yield resp
      }
    }

  private def getAnswerEndpoint(answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "answers" / LongVar(id) =>
        answerService.get(id).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(AnswerNotFoundError) => NotFound("The answer was not found.")
        }
    }

  private def listAnswersFromPoll(answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case GET -> Root / "polls" / LongVar(pollId) / "answers" =>
        for {
          retrieved <- answerService.list(pollId)
          resp <- Ok(retrieved.asJson)
        } yield resp
    }

  private def voteAnswerEndpoint(answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case PUT -> Root / "answers" / LongVar(id) =>
        for {
          _ <- answerService.vote(id)
          resp <- Ok()
        } yield resp
    }

  def endpoints(answerService: AnswerService[F]): HttpService[F] =
    createAnswerEndpoint(answerService) <+> getAnswerEndpoint(answerService) <+> listAnswersFromPoll(
      answerService) <+> voteAnswerEndpoint(answerService)
}

object AnswerEndpoints {
  def endpoints[F[_]: Effect](answerService: AnswerService[F]): HttpService[F] =
    new AnswerEndpoints[F].endpoints(answerService)
}
