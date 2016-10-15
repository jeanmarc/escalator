package nl.about42.experiments.routing

import akka.actor._
import akka.pattern.AskTimeoutException
import akka.routing.{BroadcastGroup, ScatterGatherFirstCompletedGroup, ScatterGatherFirstCompletedPool}
import nl.about42.actorutils.Reaper
import nl.about42.actorutils.Reaper.WatchMe

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Status.Failure


/**
  * Created by Jean-Marc van Leerdam on 2016-01-11
  */

case class Questions( questions: List[String], matchers: ActorRef)
case class MatchMessage( path: String)
case class SuccessfulMatch(handlerName: String, path: String)

import RoutingExperiment.printmessage

class Handler( name: String) extends Actor {
  def receive = {
    case MatchMessage(path) if path.contains(name) => {
      printmessage( name + " processed message " + path)
      sender() ! SuccessfulMatch(name, path)
    }
    case MatchMessage(path) => printmessage( name + " did not match " + path)
    case m => printmessage(s"$name ignored an unexpected message type (${m.getClass.getCanonicalName})")
  }
}

class Asker(name: String) extends Actor {
  def receive = {
    case Questions(list, matchers) => {
      list.foreach(q => {
        printmessage(s"$name asks $q")
        matchers ! MatchMessage(q)
      })
    }
    case SuccessfulMatch(handler, path) => printmessage(name + " got response from " + handler + " for message " + path)
    case (s: String) => printmessage( name + " got unexpected message " + s)
    case Failure(e: AskTimeoutException) => printmessage(name + " got a timeout for a question")
    case a => printmessage( name + " got an unknown message " + a)
      printmessage( "the class name is " + a.getClass.getCanonicalName)
  }
}

class RoutingReaper extends Reaper{
  def allSoulsReaped(): Unit = {
    printmessage("RoutingReaper has collected all souls, shutting down actor system")
    context.system.terminate()
  }

  def watchCount(): Int = watching.size
}

object RoutingExperiment extends App {
  val startTime = System.nanoTime()
  val system = ActorSystem("BroadcastMatcher")

  val asker = system.actorOf(Props(new Asker("oracle")), name = "asker")

  var matchers = List[ActorRef]()
  matchers = system.actorOf(Props(new Handler("h1path")), name = "h1") :: matchers
  matchers = system.actorOf(Props(new Handler("h2path")), name = "h2") :: matchers
  matchers = system.actorOf(Props(new Handler("h3path")), name = "h3") :: matchers
  matchers = system.actorOf(Props(new Handler("h4path")), name = "h4") :: matchers


  val matcherPaths = matchers.map( _.path.toString)

  // create a pool of Handlers that each watch for some matches (overlap allowed)
  val matcherPool: ActorRef = system.actorOf(ScatterGatherFirstCompletedGroup(matcherPaths, within = 1.seconds).
    props(), "matcherPool")
  //val matcherPool: ActorRef = system.actorOf(BroadcastGroup(matcherPaths).
  // props(), "matcherPool")

  // setup some guardians to see how the game is doing
  val reaper = system.actorOf(Props[RoutingReaper], name = "reaper")
  reaper ! WatchMe(asker)
  reaper ! WatchMe(matcherPool)

  asker ! Questions(List("h1path", "h2path", "h1pathh2pathh3pathh4path", "nomatch", "h4path"), matcherPool)

  // send poison pills in 15 seconds
  import system.dispatcher
  system.scheduler.scheduleOnce(5 seconds, asker, PoisonPill)
  system.scheduler.scheduleOnce(5 seconds, matcherPool, PoisonPill)

  // now wait for the game to finish
  val duration: FiniteDuration = 10 seconds

  printmessage("enter waiting loop (" + duration + ")...")
  Await.result(system.whenTerminated, duration)

  // this should be the last output of the application
  printmessage("that's all folks!")


  def printmessage(msg: String): Unit = {
    val elapsed: Double = (System.nanoTime() - startTime) / 1000000.0
    println(f"$elapsed%1.5f $msg")

  }
}