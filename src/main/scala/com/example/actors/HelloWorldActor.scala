package com.example.actors

import akka.actor.Actor
import akka.actor.Props

class HelloWorldActor extends Actor {

  override def preStart(): Unit = {
    val greeter = context.actorOf(Props[MatchEvaluator], "matchEvaluator")
    greeter ! MatchEvaluator.Match(300000, 1800000)
  }

  def receive = {
    case r: MatchEvaluator.MatchResult =>
      context.stop(self)
  }
}

