package io.github.cnguy.strawpoll.domain.polls

import scala.language.higherKinds
import cats.Monad
import cats.data.EitherT
import io.github.cnguy.strawpoll.domain.PollNotFoundError
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerRepositoryAlgebra}

class PollService[F[_]](pollRepo: PollRepositoryAlgebra[F], answerRepo: AnswerRepositoryAlgebra[F]) {
  import cats.syntax.all._

  def createPoll(poll: Poll, answers: List[Answer]): F[Poll] = {
    val saved = pollRepo.create(poll)
    val _ = answerRepo.createBatch(answers)
    saved
  }

  def get(id: Long)(implicit M: Monad[F]): EitherT[F, PollNotFoundError.type, Poll] =
    EitherT.fromOptionF(pollRepo.get(id), PollNotFoundError)

  def delete(id: Long)(implicit M: Monad[F]): F[Unit] =
    pollRepo.delete(id).as(())
}

object PollService {
  def apply[F[_]](pollRepo: PollRepositoryAlgebra[F], answerRepo: AnswerRepositoryAlgebra[F]): PollService[F] =
    new PollService(pollRepo, answerRepo)
}
