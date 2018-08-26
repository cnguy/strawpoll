package io.github.cnguy.strawpoll.domain.answers

import scala.language.higherKinds

trait AnswerRepositoryAlgebra[F[_]] {
  def create(answer: Answer): F[Answer]
  def get(answerId: Long): F[Option[Answer]]
  def list(pollId: Long): F[List[Answer]]
  def vote(answerId: Long): F[Option[Answer]]
  def delete(answerId: Long): F[Option[Answer]]
}
