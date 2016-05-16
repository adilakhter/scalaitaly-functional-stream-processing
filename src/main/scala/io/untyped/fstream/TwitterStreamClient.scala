package io.untyped.fstream

import twitter4j.{ StallWarning, _ }

import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.stream.async.mutable.Queue

object TwitterStreamClient extends TwitterConfig {
  def stream(q: Queue[Status]) = Task {
    val factory = new TwitterStreamFactory(twitterConfig)
    val twitterStream: TwitterStream = factory.getInstance()
    twitterStream.addListener(twitterStatusListener(q))
    //twitterStream.filter(query) // does not seems to work well for the demo
    twitterStream.sample("en")
  }

  def twitterStatusListener(q: Queue[Status]) = new StatusListener {
    def onStatus(status: Status) = {
      (Process(status) to q.enqueue).run.run
    }

    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

    def onException(ex: Exception) {
      ex.printStackTrace()
    }

    def onScrubGeo(arg0: Long, arg1: Long) {}

    def onStallWarning(warning: StallWarning) {}
  }

  def stop(twitterStream: TwitterStream) = Task.delay {
    twitterStream.cleanUp()
    twitterStream.shutdown()
  }
}
