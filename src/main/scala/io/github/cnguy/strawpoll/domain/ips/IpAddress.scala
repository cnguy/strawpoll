package io.github.cnguy.strawpoll.domain.ips

final case class IpAddress(
    answerId: Long,
    value: String,
    id: Option[Long] = None
)
