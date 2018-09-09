package io.github.cnguy.strawpoll.domain.ips

import scala.language.higherKinds

trait IpAddressRepositoryAlgebra[F[_]] {
  def create(ipAddress: IpAddress): F[IpAddress]
  def list(answerId: Long): F[List[IpAddress]]
}
