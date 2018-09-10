package io.github.cnguy.strawpoll.domain.ips

import scala.language.higherKinds

class IpAddressService[F[_]](ipAddressRepo: IpAddressRepositoryAlgebra[F]) {
  def list(answerId: Long): F[List[IpAddress]] =
    ipAddressRepo.list(answerId)

  def create(ipAddress: IpAddress): F[IpAddress] = ipAddressRepo.create(ipAddress)
}

object IpAddressService {
  def apply[F[_]](ipAddressRepo: IpAddressRepositoryAlgebra[F]): IpAddressService[F] =
    new IpAddressService(ipAddressRepo)
}
