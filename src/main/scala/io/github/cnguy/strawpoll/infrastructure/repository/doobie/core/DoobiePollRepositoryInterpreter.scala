package io.github.cnguy.strawpoll.infrastructure.repository.doobie.core

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.github.cnguy.strawpoll.domain.polls.{Poll, PollRepositoryAlgebra}

private object PollSQL {
  def select(pollId: Long): Query0[Poll] = sql"""
    SELECT QUESTION, ID
    FROM POLLS
    WHERE ID = $pollId
  """.query[Poll]

  def insert(poll: Poll): Update0 = sql"""
    INSERT INTO POLLS (QUESTION)
    VALUES (${poll.question})
  """.update

  def delete(pollId: Long): Update0 = sql"""
    DELETE FROM POLLS
    WHERE ID = $pollId
  """.update
}

class DoobiePollRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends PollRepositoryAlgebra[F] {

  def create(poll: Poll): F[Poll] =
    PollSQL
      .insert(poll)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => poll.copy(id = id.some))
      .transact(xa)

  def get(pollId: Long): F[Option[Poll]] =
    PollSQL.select(pollId).option.transact(xa)

  def delete(pollId: Long): F[Option[Poll]] =
    OptionT(get(pollId)).semiflatMap(poll => PollSQL.delete(pollId).run.transact(xa).as(poll)).value
}

object DoobiePollRepositoryInterpreter {
  def apply[F[_]: Monad](
      xa: Transactor[F]
  ): DoobiePollRepositoryInterpreter[F] =
    new DoobiePollRepositoryInterpreter[F](xa)
}
