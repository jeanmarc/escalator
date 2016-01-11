package nl.about42.experiments.routing

import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import akka.routing.{BroadcastGroup, ScatterGatherFirstCompletedGroup, ScatterGatherFirstCompletedPool}
import nl.about42.actorutils.Reaper
import nl.about42.actorutils.Reaper.WatchMe

import scala.concurrent.Await
import scala.concurrent.duration._


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
    case _ => println(name + " ignored a message")
  }
}

class Asker(name: String) extends Actor {
  def receive = {
    case Questions(list, matchers) => {
      list.foreach( matchers ! MatchMessage(_))
    }
    case SuccessfulMatch(handler, path) => println("got response from " + handler + " for message " + path)
    case _ => println( "got an unexpected message")
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

  // now wait for the game to finish
  println("enter waiting loop (1 minute)...")
  Await.result(system.whenTerminated, 1 minute)

  // this should be the last output of the application
  println("that's all folks!")


}