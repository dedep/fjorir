package com.example.actors

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{ActorLogging, Props, Actor}
import com.example.actors.MatchReferee.Match

import scala.concurrent.duration._

object MatchReferee {
  case class Match(hostId: Int, awayId: Int, playDate: LocalDateTime)
}

class MatchReferee extends Actor with ActorLogging {

  implicit val executor = context.system.dispatcher

  override def receive: Receive = {
    case m: Match =>
      val msg = MatchEvaluator.Match(200, 300)
      val recipient = context.actorOf(Props[MatchEvaluator], "match")
      val delay = getDelayTo(m.playDate)
      log.info(s"Scheduling match $msg to actor ${recipient.path} within $delay.")

      context.system.scheduler.scheduleOnce(delay, recipient, msg)
  }

  private def getDelayTo(date: LocalDateTime): FiniteDuration = {
    val now = LocalDateTime.now()

    if (date isBefore now) 0 millis
    else ChronoUnit.MILLIS.between(now, date) millis
  }
}
