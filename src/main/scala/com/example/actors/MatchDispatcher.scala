package com.example.actors

import java.time.LocalDateTime

import akka.actor.{ActorLogging, Terminated, Props, Actor}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.example.actors.MatchDispatcher.{MatchFinished, DispatchMatch}

object MatchDispatcher {
  case class DispatchMatch(id: String, hostValue: Double, awayValue: Double, date: LocalDateTime)
  case class MatchFinished(id: String)
}

class MatchDispatcher extends Actor with ActorLogging {

  val SCHEDULERS_SIZE = 5

  var router = {
    val routees = (1 to SCHEDULERS_SIZE).map(i => {
      val r = context.actorOf(Props[MatchScheduler], s"scheduler-$i")
      context watch r
      ActorRefRoutee(r)
    })

    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive: Receive = {
    case m: DispatchMatch =>
      log.info(s"Dispatching match $m")
      val msg = MatchScheduler.Match.tupled(DispatchMatch.unapply(m).getOrElse(throw new IllegalStateException()))
      router.route(msg, sender())

    case MatchFinished(id) =>
      log.info(s"Match $id finished")

    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[MatchScheduler], a.path.name)
      context watch r
      router = router.addRoutee(r)
  }
}
