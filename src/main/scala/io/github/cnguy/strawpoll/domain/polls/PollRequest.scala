package io.github.cnguy.strawpoll.domain.polls

import io.github.cnguy.strawpoll.domain.answers.Answer

// This is the interface that the client must adhere to.
// It's an intermediary, helper class for the client and `Poll` interaction.
final case class PollRequest(
    question: String,
    securityType: Option[PollSecurityType],
    answers: List[Answer]
) {
  def toPoll: Poll = Poll(question, securityType)
}
