package com.example.actors

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.Actor
import akka.actor.Props

class HelloWorldActor extends Actor {

  override def preStart(): Unit = {
    val greeter = context.actorOf(Props[MatchReferee], "referee")
    greeter ! MatchReferee.Match(UUID.randomUUID().toString,
      1, 2, LocalDateTime.now().plusSeconds(20))
  }

  def receive = {
    case r: MatchEvaluator.MatchResult =>
      context.stop(self)
  }
}
