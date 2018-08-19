package io.github.cnguy.strawpoll.config

import cats.effect.Sync
import cats.implicits._
import pureconfig.error.ConfigReaderException

case class SiteConfig(db: DatabaseConfig)

object SiteConfig {

  import pureconfig._

  /**
    * Loads the site config using PureConfig.  If configuration is invalid we will
    * return an error.  This should halt the application from starting up.
    */
  def load[F[_]](implicit E: Sync[F]): F[SiteConfig] =
    E.delay(loadConfig[SiteConfig]("site")).flatMap {
      case Right(ok) => E.pure(ok)
      case Left(e) => E.raiseError(new ConfigReaderException[SiteConfig](e))
    }
}
