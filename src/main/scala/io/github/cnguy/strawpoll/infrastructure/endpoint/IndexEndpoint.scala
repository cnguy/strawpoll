package io.github.cnguy.strawpoll.infrastructure.endpoint

import scala.language.higherKinds
import cats.effect.{Effect}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import java.io.File

class IndexEndpoint[F[_]: Effect] extends Http4sDsl[F] {
  def showIndexEndpoint(): HttpService[F] =
    HttpService[F] {
      // All this is just a hack to get SPA working. It is definitely not efficient,
      // but I want to keep the app self-contained for now.
      case request @ GET -> Root =>
        StaticFile
          .fromFile(
            new File(getClass.getClassLoader.getResource("frontend/client/src/index.html").getPath),
            Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root / "p" / LongVar(_) =>
        StaticFile
          .fromFile(
            new File(getClass.getClassLoader.getResource("frontend/client/src/index.html").getPath),
            Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root / "r" / LongVar(_) =>
        StaticFile
          .fromFile(
            new File(getClass.getClassLoader.getResource("frontend/client/src/index.html").getPath),
            Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root / "build" / "Index.js" =>
        StaticFile
          .fromFile(
            new File(getClass.getClassLoader.getResource("frontend/client/build/Index.js").getPath),
            Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root / "Index.css" =>
        StaticFile
          .fromFile(
            new File(getClass.getClassLoader.getResource("frontend/client/src/Index.css").getPath),
            Some(request))
          .getOrElseF(NotFound())
    }

  def endpoints(): HttpService[F] =
    showIndexEndpoint()
}

object IndexEndpoint {
  def endpoints[F[_]: Effect](): HttpService[F] =
    new IndexEndpoint[F].endpoints()
}
