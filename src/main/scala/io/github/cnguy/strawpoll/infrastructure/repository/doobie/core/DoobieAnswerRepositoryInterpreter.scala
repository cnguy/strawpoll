package io.github.cnguy.strawpoll.infrastructure.repository.doobie.core

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.github.cnguy.strawpoll.domain.answers.{Answer, AnswerRepositoryAlgebra}
import java.util.UUID

private object AnswerSQL {
  def select(answerId: Long): Query0[Answer] = sql"""
    SELECT POLL_ID, RESPONSE, RANK, COUNT, ID
    FROM ANSWERS
    WHERE ID = $answerId
  """.query[Answer]

  def selectByPoll(pollId: Long): Query0[Answer] = sql"""
    SELECT POLL_ID, RESPONSE, RANK, COUNT, ID
    FROM ANSWERS
    WHERE POLL_ID = $pollId
  """.query[Answer]

  def insertBatch: Update[Answer] =
    Update[Answer](
      "INSERT INTO ANSWERS (POLL_ID, RESPONSE, RANK, COUNT, ID) VALUES (?, ?, ?, ?, ?)")

  def vote(answerId: Long): Update0 = sql"""
    UPDATE ANSWERS
    SET COUNT = COUNT + 1
    WHERE ID = $answerId
  """.update
}

class DoobieAnswerRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends AnswerRepositoryAlgebra[F] {
  def get(answerId: Long): F[Option[Answer]] =
    AnswerSQL.select(answerId).option.transact(xa)

  def list(pollId: Long): F[List[Answer]] =
    AnswerSQL.selectByPoll(pollId).to[List].transact(xa)

  def createBatchForPoll(pollId: Long, answers: List[Answer]): F[List[Answer]] =
    AnswerSQL.insertBatch
      .updateManyWithGeneratedKeys[Answer](
        "POLL_ID",
        "RESPONSE",
        "RANK",
        "COUNT",
        "ID"
      )(
        answers.map(
          answer =>
            answer.copy(
              pollId = pollId,
              id = Some((UUID.randomUUID.getMostSignificantBits & Long.MaxValue) / 100000))))
      .compile
      .toList
      .transact(xa)

  def vote(answerId: Long): F[Option[Answer]] =
    OptionT(get(answerId))
      .semiflatMap(answer => AnswerSQL.vote(answerId).run.transact(xa).as(answer))
      .value
}

object DoobieAnswerRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieAnswerRepositoryInterpreter[F] =
    new DoobieAnswerRepositoryInterpreter[F](xa)
}
