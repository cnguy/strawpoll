package io.github.cnguy.strawpoll.domain.polls

import scala.language.higherKinds
import io.github.cnguy.strawpoll.domain.answers.AnswerWithNoPollId

trait PollRepositoryAlgebra[F[_]] {
  def create(poll: Poll, answer: List[AnswerWithNoPollId]): F[Poll]
  def get(pollId: Long): F[Option[Poll]]
  def delete(pollId: Long): F[Option[Poll]]
}
