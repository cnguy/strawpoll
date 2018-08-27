package io.github.cnguy.strawpoll.domain.answers

import scala.language.higherKinds

class AnswerService[F[_]](answerRepo: AnswerRepositoryAlgebra[F]) {
  def list(pollId: Long): F[List[Answer]] =
    answerRepo.list(pollId)

  def createMultipleAnswersForPoll(
      pollId: Long,
      answers: List[AnswerWithNoPollId]): F[List[Answer]] =
    answerRepo.createBatchForPoll(pollId, answers)

  def vote(id: Long): F[Option[Answer]] =
    answerRepo.vote(id)
}

object AnswerService {
  def apply[F[_]](answerRepo: AnswerRepositoryAlgebra[F]): AnswerService[F] =
    new AnswerService(answerRepo)
}
