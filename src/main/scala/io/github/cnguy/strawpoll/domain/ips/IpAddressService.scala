package io.github.cnguy.strawpoll.domain.ips

import scala.language.higherKinds

class IpAddressService[F[_]](ipAddressRepo: IpAddressRepositoryAlgebra[F]) {
  def create(ipAddress: IpAddress): F[IpAddress] = ipAddressRepo.create(ipAddress)

  def list(answerId: Long): F[List[IpAddress]] =
    ipAddressRepo.list(answerId)
}

object IpAddressService {
  def apply[F[_]](ipAddressRepo: IpAddressRepositoryAlgebra[F]): IpAddressService[F] =
    new IpAddressService(ipAddressRepo)
}
