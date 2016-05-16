package io.untyped.fstream
package examples

import twitter4j.{ TwitterFactory, _ }

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.concurrent.Task
import scalaz.stream.async.mutable.Queue
import scalaz.stream.{ Exchange, Process, time, _ }
import _root_.io.untyped.fstream.TwitterConfig

import Process._

object AsyncAwaitDemo extends TwitterConfig {
  def run() {
    val twitterClient = new TwitterFactory(twitterConfig).getInstance()
    def statuses(query: Query): Task[List[Status]] =
      Task {
        twitterClient
          .search(query)
          .getTweets
          .toList
      }

    def buildSearchProcess(query: Query): Process[Task, Status] = {
      val queryTask: Task[List[Status]] = statuses(query)
      await(queryTask)(statusList ⇒ Process.emitAll(statusList))
    }

    val akkaSearchProcess: Process[Task, Status] =
      buildSearchProcess(new Query(Akka.toString))

    akkaSearchProcess.runLog.run
  }
}
