package io.github.cnguy.strawpoll.infrastructure.repository.doobie.core

import cats._
import doobie._
import doobie.implicits._
import io.github.cnguy.strawpoll.domain.ips.{IpAddress, IpAddressRepositoryAlgebra}

private object IpAddressSQL {
  def selectByAnswer(answerId: Long): Query0[IpAddress] = sql"""
    SELECT ANSWER_ID, VALUE, ID
    FROM IP_ADDRESSES
    WHERE ANSWER_ID = $answerId
  """.query[IpAddress]

  def insert(ipAddress: IpAddress): Update0 = sql"""
    INSERT INTO IP_ADDRESSES (ANSWER_ID, VALUE)
    VALUES (${ipAddress.answerId}, ${ipAddress.value})
  """.update
}

class DoobieIpAddressRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
    extends IpAddressRepositoryAlgebra[F] {
  import cats.implicits._

  def list(answerId: Long): F[List[IpAddress]] =
    IpAddressSQL.selectByAnswer(answerId).to[List].transact(xa)

  def create(ipAddress: IpAddress): F[IpAddress] =
    IpAddressSQL
      .insert(ipAddress)
      .withUniqueGeneratedKeys[Long]("ID")
      .map((id: Long) => ipAddress.copy(id = id.some))
      .transact(xa)
}

object DoobieIpAddressRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieIpAddressRepositoryInterpreter[F] =
    new DoobieIpAddressRepositoryInterpreter(xa)
}
