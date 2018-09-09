package io.github.cnguy.strawpoll.domain.polls

import enumeratum._

final case class Poll(
    question: String,
    securityType: Option[PollSecurityType] = None,
    id: Option[Long] = None
)

sealed trait PollSecurityType extends EnumEntry

case object PollSecurityType extends Enum[PollSecurityType] with CirceEnum[PollSecurityType] {
  case object IpAddressCheck extends PollSecurityType
  case object BrowserCookieCheck extends PollSecurityType

  val values = findValues
}
