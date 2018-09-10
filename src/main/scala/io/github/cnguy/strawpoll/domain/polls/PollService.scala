package io.github.cnguy.strawpoll.domain.polls

import scala.language.higherKinds
import cats.Monad
import cats.data.EitherT
import io.github.cnguy.strawpoll.domain.PollNotFoundError

class PollService[F[_]](pollRepo: PollRepositoryAlgebra[F]) {
  import cats.syntax.all._

  def get(id: Long)(implicit M: Monad[F]): EitherT[F, PollNotFoundError.type, Poll] =
    EitherT.fromOptionF(pollRepo.get(id), PollNotFoundError)

  def create(poll: Poll): F[Poll] =
    pollRepo.create(poll)

  def delete(id: Long)(implicit M: Monad[F]): F[Unit] =
    pollRepo.delete(id).as(())
}

object PollService {
  def apply[F[_]](pollRepo: PollRepositoryAlgebra[F]): PollService[F] =
    new PollService(pollRepo)
}
