package io.github.cnguy.strawpoll.domain.polls

final case class Poll(
    question: String,
    id: Option[Long] = None
)
