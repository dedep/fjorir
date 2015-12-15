package com.example.actors

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{ActorLogging, Props}
import akka.persistence.{RecoveryCompleted, SnapshotOffer, PersistentActor}
import com.example.actors.MatchScheduler.{PersistSnapshot, Match}

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.language.postfixOps

object MatchScheduler {
  case class Match(id: String, hostValue: Double, awayValue: Double, playDate: LocalDateTime)
  object PersistSnapshot
}

class MatchScheduler extends PersistentActor with ActorLogging {

  private var matchQueue: Queue[Match] = Queue.empty

  implicit val executor = context.system.dispatcher

  override def receiveRecover: Receive = {
    case m: Match =>
      matchQueue = matchQueue :+ m

    case r: MatchEvaluator.MatchResult =>
      matchQueue = matchQueue.filterNot(_.id == r.id)

    case SnapshotOffer(_, snap: Queue[Match]) =>
      matchQueue = snap

    case RecoveryCompleted =>
      matchQueue.foreach(scheduleMatch)
  }

  override def receiveCommand: Receive = {
    case m: Match =>
      matchQueue = matchQueue :+ m
      persist(m)(scheduleMatch)

    case r: MatchEvaluator.MatchResult =>
      matchQueue = matchQueue.filterNot(_.id == r.id)
      persist(r)(r =>
        context.parent ! MatchDispatcher.MatchFinished(r.id)
      )

    case PersistSnapshot =>
      saveSnapshot(matchQueue)
  }

  override val persistenceId: String = context.self.path.name

  private val scheduleMatch: (Match) => Unit = {
    m: Match =>
      val msg = MatchEvaluator.Match(m.id, m.hostValue, m.awayValue)
      val recipient = context.actorOf(Props[MatchEvaluator], s"match-${m.id}")
      val delay = getDelayTo(m.playDate)
      log.debug(s"Scheduling match $msg to actor ${recipient.path} within $delay.")

      context.system.scheduler.scheduleOnce(delay, recipient, msg)
  }

  private def getDelayTo(date: LocalDateTime): FiniteDuration = {
    val now = LocalDateTime.now()

    if (date isBefore now) 0 millis
    else ChronoUnit.MILLIS.between(now, date) millis
  }
}
