package io.github.cnguy.strawpoll.domain.answers

import scala.language.higherKinds

import cats.Monad
import cats.data.EitherT
import io.github.cnguy.strawpoll.domain.AnswerNotFoundError

class AnswerService[F[_]](answerRepo: AnswerRepositoryAlgebra[F]) {
  def get(answerId: Long)(implicit M: Monad[F]): EitherT[F, AnswerNotFoundError.type, Answer] =
    EitherT.fromOptionF(answerRepo.get(answerId), AnswerNotFoundError)

  def list(pollId: Long): F[List[Answer]] =
    answerRepo.list(pollId)

  def createMultipleAnswersForPoll(pollId: Long, answers: List[Answer]): F[List[Answer]] =
    answerRepo.createBatchForPoll(pollId, answers)

  def vote(id: Long): F[Option[Answer]] =
    answerRepo.vote(id)
}

object AnswerService {
  def apply[F[_]](answerRepo: AnswerRepositoryAlgebra[F]): AnswerService[F] =
    new AnswerService(answerRepo)
}
