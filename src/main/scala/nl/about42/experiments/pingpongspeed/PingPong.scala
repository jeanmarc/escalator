package nl.about42.experiments.pingpongspeed

import java.util.{Calendar, Date}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import nl.about42.experiments.Reaper
import nl.about42.experiments.Reaper.WatchMe

import scala.concurrent.Await
import scala.concurrent.duration._

case object PingMessage
case object PongMessage
case object StartMessage
case object StopMessage

/**
  * An Akka Actor example written by Alvin Alexander of
  * <a href="http://devdaily.com" title="http://devdaily.com">http://devdaily.com</a>
  *
  * Shared here under the terms of the Creative Commons
  * Attribution Share-Alike License: <a href="http://creativecommons.org/licenses/by-sa/2.5/" title="http://creativecommons.org/licenses/by-sa/2.5/">http://creativecommons.org/licenses/by-sa/2.5/</a>
  *
  * more akka info: <a href="http://doc.akka.io/docs/akka/snapshot/scala/actors.html" title="http://doc.akka.io/docs/akka/snapshot/scala/actors.html">http://doc.akka.io/docs/akka/snapshot/scala/actors.html</a>
  */
class Ping(pong: ActorRef, expiration: Date) extends Actor {
  val expirationDate = expiration
  var count = 0

  def receive = {
    case StartMessage =>
      count += 1
      pong ! PingMessage
    case PongMessage =>
      if (count % 10000 == 0 && expirationDate.before(Calendar.getInstance().getTime())) {
        sender ! StopMessage
        println(s"time's up, stopping after ${count} pingpongs")
        context.stop(self)
      } else {
        count += 1
        sender ! PingMessage
      }
  }
}

class Pong extends Actor {
  def receive = {
    case PingMessage =>
      sender ! PongMessage
    case StopMessage =>
      println("pong stopped")
      context.stop(self)
  }
}

class PingPongReaper extends Reaper{
  def allSoulsReaped(): Unit = {
    println("PingPongReaper has collected all souls, shutting down actor system")
    context.system.terminate()
  }

  def watchCount(): Int = watching.size
}

object PingPong extends App {

  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong], name = "pong")
  var endTime = Calendar.getInstance
    endTime.add(Calendar.SECOND, 10)
  val ping = system.actorOf(Props(new Ping(pong, endTime.getTime())), name = "ping")

  // setup some guardians to see how the game is doing
  val reaper = system.actorOf(Props[PingPongReaper], name = "reaper")
  reaper ! WatchMe(pong)
  reaper ! WatchMe(ping)

  // start them going
  ping ! StartMessage

  // now wait for the game to finish
  println("enter waiting loop...")
  Await.result(system.whenTerminated, 1 days)

  // this should be the last output of the application
  println("that's all folks!")
}
