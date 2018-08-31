package io.github.cnguy.strawpoll

import config.{DatabaseConfig, SiteConfig}
import infrastructure.endpoint._
import infrastructure.repository.doobie.core._
import cats.effect._
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.github.cnguy.strawpoll.domain.answers.AnswerService
import io.github.cnguy.strawpoll.domain.polls.PollService
import io.github.cnguy.strawpoll.infrastructure.repository.doobie.core._
import org.http4s.server.blaze.BlazeBuilder

object Server extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, shutdown)

  def createStream[F[_]](args: List[String], shutdown: F[Unit])(
      implicit E: Effect[F]): Stream[F, ExitCode] =
    for {
      conf <- Stream.eval(SiteConfig.load[F])
      xa <- Stream.eval(DatabaseConfig.dbTransactor(conf.db))
      _ <- Stream.eval(DatabaseConfig.initializeDb(conf.db, xa))
      answerRepo = DoobieAnswerRepositoryInterpreter[F](xa)
      pollRepo = DoobiePollRepositoryInterpreter[F](xa)
      answerService = AnswerService[F](answerRepo)
      pollService = PollService[F](pollRepo)
      exitCode <- BlazeBuilder[F]
        .bindHttp(sys.env.getOrElse("PORT", "8080").toInt, "0.0.0.0")
        .mountService(IndexEndpoint.endpoints[F](), "/")
        .mountService(AnswerEndpoints.endpoints[F](answerService), "/api")
        .mountService(PollEndpoints.endpoints[F](pollService, answerService), "/api")
        .serve
    } yield exitCode
}
