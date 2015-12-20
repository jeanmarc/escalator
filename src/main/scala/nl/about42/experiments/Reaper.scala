package nl.about42.experiments

import akka.actor.{Terminated, Actor, ActorRef}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by jml on 12/20/15.
  */
object Reaper {
  case class WatchMe(ref:ActorRef)
}

abstract class Reaper extends Actor {
  import Reaper._

  val watching = ArrayBuffer.empty[ActorRef]

  // this method is called when all actors in watching have died
  def allSoulsReaped(): Unit

  final def receive = {
    case WatchMe(ref) =>
      context.watch(ref)
      watching += ref
    case Terminated(ref) =>
      watching -= ref
      if(watching.isEmpty) allSoulsReaped()
  }
}

