package io.github.cnguy.strawpoll.infrastructure.repository.doobie.core

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.github.cnguy.strawpoll.domain.polls.{Poll, PollRepositoryAlgebra, PollSecurityType}

private object PollSQL {
  implicit val SecurityTypeMeta: Meta[PollSecurityType] =
    Meta[String].xmap(PollSecurityType.withName, _.entryName)

  def select(pollId: Long): Query0[Poll] = sql"""
    SELECT QUESTION, SECURITY_TYPE, ID
    FROM POLLS
    WHERE ID = $pollId
  """.query[Poll]

  def insert(poll: Poll): Update0 = sql"""
    INSERT INTO POLLS (QUESTION, SECURITY_TYPE)
    VALUES (${poll.question}, ${poll.securityType})
  """.update

  def delete(pollId: Long): Update0 = sql"""
    DELETE FROM POLLS
    WHERE ID = $pollId
  """.update
}

class DoobiePollRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends PollRepositoryAlgebra[F] {
  def get(pollId: Long): F[Option[Poll]] =
    PollSQL.select(pollId).option.transact(xa)

  def create(poll: Poll): F[Poll] =
    PollSQL
      .insert(poll)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => poll.copy(id = id.some))
      .transact(xa)

  def delete(pollId: Long): F[Option[Poll]] =
    OptionT(get(pollId)).semiflatMap(poll => PollSQL.delete(pollId).run.transact(xa).as(poll)).value
}

object DoobiePollRepositoryInterpreter {
  def apply[F[_]: Monad](
      xa: Transactor[F]
  ): DoobiePollRepositoryInterpreter[F] =
    new DoobiePollRepositoryInterpreter[F](xa)
}
