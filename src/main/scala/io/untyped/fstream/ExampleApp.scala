package io.untyped.fstream

import java.util.concurrent.Executors

import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.server.ServerBuilder
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{ HttpService, Service }
import org.log4s.getLogger

import scala.util.Properties.envOrNone

class ExampleApp(host: String, port: Int) {
  private val logger = getLogger
  private val pool = Executors.newCachedThreadPool()

  logger.info(s"Starting FStream blaze Server on '$host:$port'")

  // build our routes
  def rhoRoutes = new RhoRoutes().toService(SwaggerSupport())

  // our routes can be combinations of any HttpService, regardless of where they come from
  val routes = Service.withFallback(rhoRoutes)(new Routes().service)

  // Add some logging to the service
  val service: HttpService = routes.local { req ⇒
    val path = req.uri.path
    logger.info(s"${req.remoteAddr.getOrElse("null")} -> ${req.method}: $path")
    req
  }

  // Construct the blaze pipeline.
  def build(): ServerBuilder =
    BlazeBuilder
      .bindHttp(port, host)
      .mountService(service)
      .withServiceExecutor(pool)
}

object ExampleApp {
  val ip = "0.0.0.0"
  val port = envOrNone("HTTP_PORT") map (_.toInt) getOrElse (8080)

  def main(args: Array[String]): Unit = {
    new ExampleApp(ip, port)
      .build()
      .run
      .awaitShutdown()
  }
}

