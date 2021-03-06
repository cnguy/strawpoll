package io.github.cnguy.strawpoll.domain.polls

import scala.language.higherKinds

trait PollRepositoryAlgebra[F[_]] {
  def get(pollId: Long): F[Option[Poll]]
  def create(poll: Poll): F[Poll]
  def delete(pollId: Long): F[Option[Poll]]
}
