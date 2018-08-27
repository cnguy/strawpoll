package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.Effect
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerService}

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

  private def voteAnswerEndpoint(answerService: AnswerService[F]): HttpService[F] =
    HttpService[F] {
      case PUT -> Root / "answers" / LongVar(id) =>
        for {
          answer <- answerService.vote(id)
          resp <- answer match {
            case Some(a) => Ok(a.copy(count = a.count + 1).asJson)
            case None => NotFound()
          }
        } yield resp
    }

  def endpoints(answerService: AnswerService[F]): HttpService[F] =
    listAnswersFromPoll(answerService) <+> voteAnswerEndpoint(answerService)
}

object AnswerEndpoints {
  def endpoints[F[_]: Effect](answerService: AnswerService[F]): HttpService[F] =
    new AnswerEndpoints[F].endpoints(answerService)
}
