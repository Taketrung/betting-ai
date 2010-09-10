package dk.bettingai.marketsimulator.betex.api

import java.util.Date
import IBet.BetTypeEnum._
import IMarket._
/**This trait represents a market on a betting exchange. Market is a place that bets can be placed on, for example football match between Man Utd and Arsenal.
 * 
 * @author korzekwad
 *
 */
object IMarket {
	trait IRunner {
		val runnerId:Long
		val runnerName:String
	}
	
	/**This trait represents total unmatched volume to back and to lay at a given price.*/ 
	trait IRunnerPrice {
		val price:Double
		val totalToBack:Double
		val totalToLay: Double
	}
		
}
trait IMarket {
	
	val marketId:Long
	val marketName:String
	val eventName:String
	val numOfWinners:Int
	val marketTime:Date
	val runners:List[IRunner]
	
	/** Places a bet on a betting exchange market.
	 * 
	* @param betId
	* @param userId
	* @param betSize
	* @param betPrice
	* @param betType
	* @param runnerId
	*/
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long)
	
	/** Cancels a bet on a betting exchange market.
	 * 
	 * @param betId Unique id of a bet to be cancelled.
	 * 
	 * @return amount cancelled
	*/
	def cancelBet(betId:Long):Double
	
	/** Cancels bets on a betting exchange market.
	 * 
	 * @param userId 
	 * @param betsSize Total size of bets to be cancelled.
	 * @param betPrice The price that bets are cancelled on.
	 * @param betType
	 * @param runnerId 
	 * 
	 * @return Amount cancelled. Zero is returned if nothing is available to cancel.
	*/
	def cancelBets(userId:Long,betsSize:Double,betPrice:Double,betType:BetTypeEnum,runnerId:Long):Double
	
	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
   * 
   * @param runnerId Unique runner id that runner prices are returned for.
   * @return
   */
	def getRunnerPrices(runnerId:Long):List[IRunnerPrice]
	
	/**Returns best toBack/toLay prices for market runner.
	 * Element 1 - best price to back, element 2 - best price to lay
	 * Double.NaN is returned if price is not available.
	 * @return 
	 * */
	def getBestPrices(runnerId: Long): Tuple2[Double,Double]
	
	/**Returns best toBack/toLay prices for market.
	 * 
	 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 */
	def getBestPrices():Map[Long,Tuple2[Double,Double]]
	
	/**Returns total traded volume for all prices on all runners in a market.*/
	def getRunnerTradedVolume(runnerId:Long): List[IRunnerTradedVolume.IPriceTradedVolume]
	
	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[IBet]
	
	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 *@param matchedBetsOnly If true then matched bets are returned only, 
	 * otherwise all unmatched and matched bets for user are returned.
	 */
	def getBets(userId:Int,matchedBetsOnly:Boolean):List[IBet]
}