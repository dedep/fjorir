package com.example.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import org.apache.commons.math3.distribution.NormalDistribution

import scala.util.Random

object MatchEvaluator {
  case class Match(hostValue: Double, awayValue: Double)
  case class MatchResult(hostGoals: Int, awayGoals: Int)
}

class MatchEvaluator extends Actor with ActorLogging {

  private val hostPremium = 0.15

  def receive = {
    case m: MatchEvaluator.Match =>
      sender ! eval(m)
  }

  def eval(m: MatchEvaluator.Match): MatchEvaluator.MatchResult = {
    require(m.hostValue > 0, "val1 cannot be negative")
    require(m.awayValue > 0, "val2 cannot be negative")

    log.info("Evaluating result of the following match: {}", m)

    val bp = calcBalancePoint(m)
    log.debug("Balance point for match: {} equals: {}", m, bp.underlying())

    val mp = drawMatchPoint(bp)
    log.debug("Match point for match: {} equals: {}", m, mp.underlying())

    val result = createResultForMatchPoint(mp)
    log.info("Match: {} result has been evaluated: {}", m, result)

    result
  }

  def calcBalancePoint(m: MatchEvaluator.Match): Double =
    (log2(m.hostValue / m.awayValue) / 2) + hostPremium

  private def log2(x: Double) = Math.log(x) / Math.log(2)

  def drawMatchPoint(balancePoint: Double): Double = Random.nextGaussian() + balancePoint

  def createResultForMatchPoint(matchPoint: Double): MatchEvaluator.MatchResult = {
    val c = drawGoalsConstantComponent
    log.debug("Goals constant equals: {}", c.underlying())

    val d = calcGoalsDiff(matchPoint)
    log.debug("Goals difference equals: {}", d.underlying())

    if (d > 0) MatchEvaluator.MatchResult(d + c, c)
    else if (d < 0) MatchEvaluator.MatchResult(c, c - d)
    else MatchEvaluator.MatchResult(c, c)
  }

  def drawGoalsConstantComponent: Int = Math.round(Math.abs(Random.nextGaussian())).toInt

  def calcGoalsDiff(matchPoint: Double): Int =
    if (matchPoint >= 0) Math.floor(3 * matchPoint).toInt
    else -Math.floor(3 * -matchPoint).toInt

  def calculateLoseProbability(m: MatchEvaluator.Match): Double =
    new NormalDistribution(calcBalancePoint(m), 1).cumulativeProbability(-0.3333333)

  def calculateDrawProbability(m: MatchEvaluator.Match): Double =
    new NormalDistribution(calcBalancePoint(m), 1).probability(-0.3333333, 0.3333333)

  def calculateWinProbability(m: MatchEvaluator.Match): Double =
    1 - new NormalDistribution(calcBalancePoint(m), 1).cumulativeProbability(0.3333333)
}