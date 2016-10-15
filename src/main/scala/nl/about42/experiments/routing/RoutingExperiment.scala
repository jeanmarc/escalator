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

class Handler( name: String) extends Actor {
  def receive = {
    case MatchMessage(path) if path.contains(name) => {
      println( name + " processed message " + path)
      sender() ! SuccessfulMatch(name, path)
    }
    case MatchMessage(path) => println( name + " did not match " + path)
    case m => println(s"$name ignored an unexpected message type (${m.getClass.getCanonicalName})")
  }
}

class Asker(name: String) extends Actor {
  def receive = {
    case Questions(list, matchers) => {
      list.foreach( matchers ! MatchMessage(_))
    }
    case SuccessfulMatch(handler, path) => println(name + " got response from " + handler + " for message " + path)
    case (s: String) => println( name + " got unexpected message " + s)
    case Failure(e: AskTimeoutException) => println(name + " got a timeout for a question")
    case a => println( name + " got an unknown message " + a)
      println( "the class name is " + a.getClass.getCanonicalName)
  }
}

class RoutingReaper extends Reaper{
  def allSoulsReaped(): Unit = {
    println("RoutingReaper has collected all souls, shutting down actor system")
    context.system.terminate()
  }

  def watchCount(): Int = watching.size
}

object RoutingExperiment extends App {
  val system = ActorSystem("BroadcastMatcher")

  val asker = system.actorOf(Props(new Asker("oracle")), name = "asker")

  var matchers = List[ActorRef]()
  matchers = system.actorOf(Props(new Handler("h1path")), name = "h1") :: matchers
  matchers = system.actorOf(Props(new Handler("h2path")), name = "h2") :: matchers
  matchers = system.actorOf(Props(new Handler("h3path")), name = "h3") :: matchers
  matchers = system.actorOf(Props(new Handler("h4path")), name = "h4") :: matchers


  val matcherPaths = matchers.map( _.path.toString)

  // create a pool of Handlers that each watch for some matches (overlap allowed)
  val matcherPool: ActorRef = system.actorOf(ScatterGatherFirstCompletedGroup(matcherPaths, within = 10.seconds).
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
  system.scheduler.scheduleOnce(15 seconds, asker, PoisonPill)
  system.scheduler.scheduleOnce(15 seconds, matcherPool, PoisonPill)

  // now wait for the game to finish
  val duration: FiniteDuration = 20 seconds

  println("enter waiting loop (" + duration + ")...")
  Await.result(system.whenTerminated, duration)

  // this should be the last output of the application
  println("that's all folks!")


}