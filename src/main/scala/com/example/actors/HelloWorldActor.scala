package com.example.actors

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.Actor
import akka.actor.Props

class HelloWorldActor extends Actor {

  override def preStart(): Unit = {
    val d = context.actorOf(Props[MatchDispatcher], "dispatcher")
    d ! MatchDispatcher.DispatchMatch(UUID.randomUUID().toString, 100, 200, LocalDateTime.now().plusSeconds(20))
    d ! MatchDispatcher.DispatchMatch(UUID.randomUUID().toString, 100, 300, LocalDateTime.now().plusSeconds(30))
    d ! MatchDispatcher.DispatchMatch(UUID.randomUUID().toString, 100, 400, LocalDateTime.now().plusSeconds(50))
  }

  def receive = {
    case r: MatchEvaluator.MatchResult =>
      context.stop(self)
  }
}
