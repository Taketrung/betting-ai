package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.BetUtil._
import dk.bettingai.marketsimulator._
import scala.collection._
import java.util.Date
import com.espertech.esper.client._

 /**Provides market data and market operations that can be used by trader to place bets on a betting exchange market.*/
  trait ITraderContext {

    val userId: Int
    val marketId: Long
    val marketName: String
    val eventName: String
    val numOfWinners: Int
    val marketTime: Date
    val runners: List[IRunner]

    val commission: Double

    /**Time stamp of market event */
    def getEventTimestamp: Long
    def setEventTimestamp(eventTimestamp: Long)
    /**Add chart value to time line chart
     * 
     * @param label Label of chart series
     * @param value Value to be added to chart series
     */
    def addChartValue(label: String, value: Double)

    /**Returns best toBack/toLay prices for market runner.
     * Element 1 - best price to back, element 2 - best price to lay
     * Double.NaN is returned if price is not available.
     * @return 
     * */
    def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice, IRunnerPrice]

    /**Returns best toBack/toLay prices for market.
     * 
     * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
     */
    def getBestPrices(): Map[Long, Tuple2[IRunnerPrice, IRunnerPrice]]

    /** Places a bet on a betting exchange market.
     * 
     * @param betSize
     * @param betPrice
     * @param betType
     * @param runnerId
     * 
     * @return The bet that was placed.
     */
    def placeBet(betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): IBet

    /** Places a bet on a betting exchange market.
     * 
     * @param betSizeLimit Total user unmatched volume that should be achieved after calling this method. 
     * For example is unmatched volume is 2 and betSizeLimit is 5 then bet with bet size 3 is placed. 
     * @param betPrice
     * @param betType
     * @param runnerId
     * 
     * @return The bet that was placed or None if nothing has been placed.
     */
    def fillBet(betSizeLimit: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): Option[IBet]

    /** Cancels a bet on a betting exchange market.
     * 
     * @param betId Unique id of a bet to be cancelled.
     * 
     * @return amount cancelled
     */
    def cancelBet(betId: Long): Double

    /**Place hedge bet on a market runner to make ifwin/iflose profits even. Either back or lay bet is placed on best available price.
     * 
     * @param runnerId
     * 
     * @return Hedge bet that was placed or none if no hedge bet was placed.
     */
    def placeHedgeBet(runnerId: Long): Option[IBet]

    /**Returns all bets placed by user on that market.
     *
     *@param matchedBetsOnly If true then matched bets are returned only, 
     * otherwise all unmatched and matched bets for user are returned.
     */
    def getBets(matchedBetsOnly: Boolean): List[IBet]

    /** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
     *  Prices with zero volume are not returned by this method.
     * 
     * @param runnerId Unique runner id that runner prices are returned for.
     * @return
     */
    def getRunnerPrices(runnerId: Long): List[IRunnerPrice]

    /**Returns total traded volume for all prices on all runners in a market.*/
    def getRunnerTradedVolume(runnerId: Long): IRunnerTradedVolume

    /**Returns total traded volume for a given runner.*/
    def getTotalTradedVolume(runnerId: Long): Double

    def risk(): MarketExpectedProfit

    /**see Kelly Criterion - http://en.wikipedia.org/wiki/Kelly_criterion.*/
    def wealth(bank: Double): MarketExpectedProfit

    /**Registers new trader and return trader context. 
     * This context can be used to trigger some custom traders that are registered manually by a master trader, 
     * e.g. when testing some evolution algorithms for which more than one trader is required.
     * @return trader context
     */
    def registerTrader(): ITraderContext
    
  }