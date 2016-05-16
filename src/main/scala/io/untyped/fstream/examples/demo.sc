
import scalaz.concurrent.Task
import scala.concurrent.duration._
import scalaz.stream._



Process.halt


import Process._
val P = Process

emit(1)

emit(1).toSource

emit(1) ++ emit(10) ++ emit(11)

val p: Process[Task, Int] = emitAll(Seq(1,5,10,20))

import Process._
def integerStream: Process[Task, Int] = {
  def next (i: Int): Process[Task,Int] = await(Task(i)){ i ⇒
    emit(i) ++ next(i + 1)
  }
  next(1)
}

val intsP = integerStream.take(10)


val task = intsP.run


intsP.runLog.run


integerStream.map(_ * 100).take(5).runLog.run

integerStream.flatMap(i ⇒ emit(-i) ++ emit(-i-1)).take(5).runLog.run


val zippedP: Process[Task, (Int, String)] = integerStream.take(2) zip emitAll(Seq ("A", "B"))

zippedP.runLog.run

import process1._
integerStream |> filter(i => i > 5) |> exists(_ == 10)


val multiply10Channel: Channel[Task, Int, Int]
= Process.constant {
  (i: Int) => Task.now(i*10)
}

val a = integerStream.take(5) through multiply10Channel runLog

a.run


val printInts: Sink[Task, Int] = Process.constant {
  (i: Int) => Task.delay { println(i) }
}

val intPwithSink: Process[Task, Unit] = integerStream.take(5) to printInts

intPwithSink.run.run


val sourceToSinkProcess = integerStream.take(5) through multiply10Channel to printInts

sourceToSinkProcess.run.run

import tee._
import Process._
val p1: Process[Task, Int] = integerStream
val p2: Process[Task, String] = emitAll(Seq("A", "B", "C"))

val teeExample1: Process[Task, Any] = (p1 tee p2)(interleave)
val teeExample2: Process[Task, Int] = (p1 tee p2)(passL) //Vector(1, 2, 3, 4)

