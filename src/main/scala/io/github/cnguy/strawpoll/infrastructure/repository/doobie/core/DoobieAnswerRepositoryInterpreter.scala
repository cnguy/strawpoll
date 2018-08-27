package io.github.cnguy.strawpoll.infrastructure.repository.doobie.core

import cats._
import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import io.github.cnguy.strawpoll.domain.answers.{
  Answer,
  AnswerRepositoryAlgebra,
  AnswerWithNoPollId
}
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

  def insert(answer: Answer): Update0 = sql"""
    INSERT INTO ANSWERS (POLL_ID, RESPONSE, RANK, COUNT)
    VALUES (${answer.pollId}, ${answer.response}, ${answer.rank}, 0)
  """.update

  def insertBatch(answers: List[Answer]): Update[Answer] =
    Update[Answer](
      "INSERT INTO ANSWERS (POLL_ID, RESPONSE, RANK, COUNT, ID) VALUES (?, ?, ?, ?, ?)")

  def vote(answerId: Long): Update0 = sql"""
    UPDATE ANSWERS
    SET COUNT = COUNT + 1
    WHERE ID = $answerId
  """.update

  def delete(answerId: Long): Update0 = sql"""
    DELETE FROM ANSWERS
    WHERE ID = $answerId
  """.update
}

class DoobieAnswerRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends AnswerRepositoryAlgebra[F] {

  def create(answer: Answer): F[Answer] =
    AnswerSQL
      .insert(answer)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => answer.copy(id = id.some))
      .transact(xa)

  def createBatchForPoll(pollId: Long, answers: List[AnswerWithNoPollId]): F[List[Answer]] = {
    val answers2 = answers.map(answer => Answer(pollId, answer.response, answer.rank))
    AnswerSQL
      .insertBatch(answers2)
      .updateManyWithGeneratedKeys[Answer](
        "POLL_ID",
        "RESPONSE",
        "RANK",
        "COUNT",
        "ID"
      )(answers2.map(answer =>
        answer.copy(id = Some(UUID.randomUUID.getMostSignificantBits & Long.MaxValue))))
      .compile
      .toList
      .transact(xa)
  }

  def get(answerId: Long): F[Option[Answer]] =
    AnswerSQL.select(answerId).option.transact(xa)

  def list(pollId: Long): F[List[Answer]] =
    AnswerSQL.selectByPoll(pollId).to[List].transact(xa)

  def vote(answerId: Long): F[Option[Answer]] =
    OptionT(get(answerId))
      .semiflatMap(answer => AnswerSQL.vote(answerId).run.transact(xa).as(answer))
      .value

  def delete(answerId: Long): F[Option[Answer]] =
    OptionT(get(answerId))
      .semiflatMap(answer => AnswerSQL.delete(answerId).run.transact(xa).as(answer))
      .value
}

object DoobieAnswerRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieAnswerRepositoryInterpreter[F] =
    new DoobieAnswerRepositoryInterpreter[F](xa)
}
