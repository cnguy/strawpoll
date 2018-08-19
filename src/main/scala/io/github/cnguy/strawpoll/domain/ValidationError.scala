package io.github.cnguy.strawpoll.domain

sealed trait ValidationError extends Product with Serializable

case object PollNotFoundError extends ValidationError
case object AnswerNotFoundError extends ValidationError
