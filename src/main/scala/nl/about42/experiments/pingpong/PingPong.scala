package nl.about42.experiments.pingpong

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import nl.about42.actorutils.Reaper
import Reaper.WatchMe

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
class Ping(pong: ActorRef) extends Actor {
  var count = 0
  def incrementAndPrint {
    count += 1;
    println("ping")
  }

  def receive = {
    case StartMessage =>
      incrementAndPrint
      pong ! PingMessage
    case PongMessage =>
      incrementAndPrint
      if (count > 99) {
        sender ! StopMessage
        println("ping stopped")
        context.stop(self)
      } else {
        sender ! PingMessage
      }
  }
}

class Pong extends Actor {
  def receive = {
    case PingMessage =>
      println("     pong")
      sender ! PongMessage
    case StopMessage =>
      println("pong stopped")
      context.stop(self)
  }
}

class PingPongReaper extends Actor with Reaper{
  override def allSoulsReaped(): Unit = {
    println("PingPongReaper has collected all souls, shutting down actor system")
    context.system.terminate()
  }

  def watchCount(): Int = watching.size
}

object PingPong extends App {

  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong], name = "pong")
  val ping = system.actorOf(Props(new Ping(pong)), name = "ping")

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