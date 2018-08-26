package io.github.cnguy.strawpoll.infrastructure.repository.doobie.core

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.github.cnguy.strawpoll.domain.polls.{Poll, PollRepositoryAlgebra}
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerRepositoryAlgebra, AnswerWithNoPollId}

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

class DoobiePollRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F], answerRepo: AnswerRepositoryAlgebra[F])
    extends PollRepositoryAlgebra[F] {

  def create(poll: Poll, answers: List[AnswerWithNoPollId]): F[Poll] = {
    PollSQL
      .insert(poll)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => {
        val newId = id.some
        val newPoll = poll.copy(id = newId)
        val as = answers.map(answer => Answer(id, answer.response, answer.rank, answer.count))
        val sql = "INSERT INTO ANSWERS (POLL_ID, RESPONSE, RANK, COUNT) VALUES (?, ?, ?, ?)"
        Update[Answer](sql).updateMany(as).transact(xa).unsa
        newPoll
      })
      .transact(xa)
  }

  def get(pollId: Long): F[Option[Poll]] =
    PollSQL.select(pollId).option.transact(xa)

  def delete(pollId: Long): F[Option[Poll]] =
    OptionT(get(pollId)).semiflatMap(poll => PollSQL.delete(pollId).run.transact(xa).as(poll)).value
}

object DoobiePollRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F], answerRepo: AnswerRepositoryAlgebra[F]): DoobiePollRepositoryInterpreter[F] =
    new DoobiePollRepositoryInterpreter[F](xa, answerRepo)
}
