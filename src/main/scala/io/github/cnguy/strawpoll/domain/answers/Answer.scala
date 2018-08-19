package io.github.cnguy.strawpoll.domain.answers

final case class Answer(
    pollId: Long,
    response: String,
    count: Int = 0,
    id: Option[Long] = None
)
