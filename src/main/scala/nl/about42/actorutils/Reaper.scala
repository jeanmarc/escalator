package nl.about42.actorutils

import akka.actor.{Actor, ActorRef, Terminated}

import scala.collection.mutable.ArrayBuffer

/**
  * Abstract Actor class that watches over a population of actors and calls allSoulsReaped
  * when all watched actors have died.
  * Implementations of this class can take action based on that event. For instance by
  * shutting down the entire actor system and stopping the application, or by performing some
  * cleanup and then respawning the main actors.
  *
  * Created by Jean-Marc van Leerdam on 2015-12-20
  */
object Reaper {
  case class WatchMe(ref:ActorRef)
  case class SoulCount()
}

/**
  * Reaper is a trait to be added to Actors
 */
trait Reaper {
  self: Actor =>
  import Reaper._

  val watching = ArrayBuffer.empty[ActorRef]

  /**
    * Method is called when all registered actors have died. Can be overridden if the default behaviour is
    * not enough.
    * Default behaviour is to terminate the actor system.
    */
  def allSoulsReaped(): Unit = {
    context.system.terminate()
  }

  final def receive: Receive = {
    case SoulCount() =>
      sender ! watching.size
    case WatchMe(ref) =>
      context.watch(ref)
      watching += ref
    case Terminated(ref) =>
      watching -= ref
      if(watching.isEmpty) allSoulsReaped()
  }
}

