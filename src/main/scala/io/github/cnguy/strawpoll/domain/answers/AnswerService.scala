package io.github.cnguy.strawpoll.domain.answers

import scala.language.higherKinds
import cats.Monad
import cats.data.EitherT
import io.github.cnguy.strawpoll.domain.AnswerNotFoundError

class AnswerService[F[_]](answerRepo: AnswerRepositoryAlgebra[F]) {
  import cats.syntax.all._

  def createAnswer(answer: Answer): F[Answer] = answerRepo.create(answer)

  def createMultipleAnswersForPoll(
      pollId: Long,
      answers: List[AnswerWithNoPollId]): F[List[Answer]] =
    answerRepo.createBatchForPoll(pollId, answers)

  def get(id: Long)(implicit M: Monad[F]): EitherT[F, AnswerNotFoundError.type, Answer] =
    EitherT.fromOptionF(answerRepo.get(id), AnswerNotFoundError)

  def list(pollId: Long): F[List[Answer]] =
    answerRepo.list(pollId)

  def vote(id: Long): F[Option[Answer]] =
    answerRepo.vote(id)

  def delete(id: Long)(implicit M: Monad[F]): F[Unit] =
    answerRepo.delete(id).as(())
}

object AnswerService {
  def apply[F[_]](answerRepo: AnswerRepositoryAlgebra[F]): AnswerService[F] =
    new AnswerService(answerRepo)
}
