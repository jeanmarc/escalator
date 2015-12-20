package nl.about42.experiments

import java.util.{Date, Calendar}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import nl.about42.experiments.Reaper.WatchMe

import scala.concurrent.Await
import scala.concurrent.duration._

case object Ping2Message
case object Pong2Message
case object Start2Message
case object Stop2Message

/**
  * An Akka Actor example written by Alvin Alexander of
  * <a href="http://devdaily.com" title="http://devdaily.com">http://devdaily.com</a>
  *
  * Shared here under the terms of the Creative Commons
  * Attribution Share-Alike License: <a href="http://creativecommons.org/licenses/by-sa/2.5/" title="http://creativecommons.org/licenses/by-sa/2.5/">http://creativecommons.org/licenses/by-sa/2.5/</a>
  *
  * more akka info: <a href="http://doc.akka.io/docs/akka/snapshot/scala/actors.html" title="http://doc.akka.io/docs/akka/snapshot/scala/actors.html">http://doc.akka.io/docs/akka/snapshot/scala/actors.html</a>
  */
class Ping2(pong: ActorRef, expiration: Date) extends Actor {
  val expirationDate = expiration
  var count = 0

  def receive = {
    case Start2Message =>
      count += 1
      pong ! Ping2Message
    case Pong2Message =>
      if (expirationDate.before(Calendar.getInstance().getTime())) {
        sender ! Stop2Message
        println(s"time's up, stopping after ${count} pingpongs")
        context.stop(self)
      } else {
        count += 1
        sender ! Ping2Message
      }
  }
}

class Pong2 extends Actor {
  def receive = {
    case Ping2Message =>
      sender ! Pong2Message
    case Stop2Message =>
      println("pong2 stopped")
      context.stop(self)
  }
}

class PingPong2Reaper extends Reaper{
  def allSoulsReaped(): Unit = {
    println("PingPong2Reaper has collected all souls, shutting down actor system")
    context.system.terminate()
  }

  def watchCount(): Int = watching.size
}

object PingPong2 extends App {

  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong2], name = "pong")
  var endTime = Calendar.getInstance
    endTime.add(Calendar.SECOND, 10)
  val ping = system.actorOf(Props(new Ping2(pong, endTime.getTime())), name = "ping")

  // setup some guardians to see how the game is doing
  val reaper = system.actorOf(Props[PingPongReaper], name = "reaper")
  reaper ! WatchMe(pong)
  reaper ! WatchMe(ping)

  // start them going
  ping ! Start2Message

  // now wait for the game to finish
  println("enter waiting loop...")
  Await.result(system.whenTerminated, 1 days)

  // this should be the last output of the application
  println("that's all folks!")
}