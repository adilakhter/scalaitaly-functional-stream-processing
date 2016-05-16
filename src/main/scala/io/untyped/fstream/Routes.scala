package io.untyped.fstream

import java.util.concurrent.Executors

import org.http4s.HttpService
import org.http4s.dsl._
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits._
import twitter4j.{ TwitterFactory, _ }

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps
import scalaz.concurrent.Task
import scalaz.stream.async.mutable.Queue
import scalaz.stream.{ Exchange, Process, time, _ }

class Routes extends TwitterConfig {
  private implicit val scheduledEC = Executors.newScheduledThreadPool(4)

  val service: HttpService = HttpService {
    case r @ GET -> Root / "websocket" ⇒
      val query = new Query("#spark")
      val src1: Process[Task, Text] = twitterSource(query)
        .observe(io.stdOutLines)
        .map(Text(_))

      WS(Exchange(src1, Process.halt))

    case r @ GET -> Root / "websocket-v2" ⇒
      //val src2: Process[Task, Text] = twitterStreamProcess.observe(io.stdOutLines).map(d ⇒ Text("response2: -> " + d))
      //WS(Exchange(src2, Process.halt))

      val src2: Process[Task, Text] =
        twitterStreamSource
          .observe(io.stdOutLines)
          .map(Text(_))

      WS(Exchange(src2, Process.halt))

    // other routes
    case r @ GET -> _ if r.pathInfo.startsWith("/static")  ⇒ static(r)

    case r @ GET -> _ if r.pathInfo.startsWith("/swagger") ⇒ swagger(r)

    case r @ GET -> _ if r.pathInfo.endsWith("/")          ⇒ service(r.withPathInfo(r.pathInfo + "index.html"))

    case r @ GET -> _ ⇒
      val rr = if (r.pathInfo.contains('.')) r else r.withPathInfo(r.pathInfo + ".html")
      views(rr)
  }

  def analyze(t: Tweet): Task[EnrichedTweet] = Task {
    EnrichedTweet(t.author, t.body, t.retweetCount, SentimentAnalyzer.sentiment(t.body))
  }
  val analysisChannel: Channel[Task, Tweet, EnrichedTweet] = channel lift analyze

  private def twitterSource(query: Query): Process[Task, String] = {
    import time._

    val twitterClient: Twitter = new TwitterFactory(twitterConfig).getInstance()
    def statuses(query: Query): Task[List[Status]] = Task {
      val receivedTweets = twitterClient.search(query).getTweets.toList
      println("Received " + receivedTweets.size + " tweets") // Logging the response from the server
      receivedTweets
    }

    val queryChannel: Channel[Task, Query, List[Status]] = channel lift statuses

    def buildTwitterQuery(query: Query): Process1[Any, Query] = process1 lift { _ ⇒ query }

    def tweetsP(query: Query): Process[Task, Status] =
      awakeEvery(5 seconds) |> buildTwitterQuery(query) through queryChannel flatMap {
        Process emitAll _
      }

    tweetsP(query).map(status ⇒
      Tweet(
        author = Author(status.getUser.getScreenName),
        retweetCount = status.getRetweetCount,
        body = status.getText)
    ) through analysisChannel map (_.toString)

  }

  private def twitterStreamSource: Process[Task, String] = {
    val size = 10
    //values read from remote system
    import TwitterStreamClient._
    val tweetsQueue: Queue[Status] = async.boundedQueue[Status](size)

    Process.eval_(stream(tweetsQueue))
      .run
      .runAsync { _ ⇒ println("All input data was written") }

    tweetsQueue.dequeue.map(status ⇒
      Tweet(
        author = Author(status.getUser.getScreenName),
        retweetCount = status.getRetweetCount,
        body = status.getText)
    ) through analysisChannel map (_.toString)

  }
}
