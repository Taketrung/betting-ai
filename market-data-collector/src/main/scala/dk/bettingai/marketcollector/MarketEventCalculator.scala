package dk.bettingai.marketcollector

import dk.bettingai.marketsimulator.betex.api.IMarket._
import dk.bettingai.marketsimulator.betex.Market._

/**This trait represents a function that calculates market events for the delta between the previous and the current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
object MarketEventCalculator  extends IMarketEventCalculator{


	/**Calculates market events for the delta between the previous and the current state of the market runner.
	 * 
	 * @param userId The user Id that the bet placement events are calculated for.
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param marketRunnerDelta Delta between the new and the previous state of the market runner (both runner prices and traded volume combined to runner prices).
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculateMarketEvents(userId:Long,marketId:Long,runnerId:Long)(marketRunnerDelta:List[IRunnerPrice]): List[String] = {

		/**Create lay bet events for delta between the new and the previous runner states.*/
		val layBetEvents:List[String] = for {
			deltaRunnerPrice <- marketRunnerDelta 
			if(deltaRunnerPrice.totalToBack != 0) 
				val placeLayBetEvent = if(deltaRunnerPrice.totalToBack>0)
					"""{"eventType":"PLACE_BET","userId":%s,"betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(userId,deltaRunnerPrice.totalToBack,deltaRunnerPrice.price,"LAY",marketId,runnerId)
					else 
						"""{"eventType":"CANCEL_BETS","userId":%s,"betsSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(userId,-deltaRunnerPrice.totalToBack,deltaRunnerPrice.price,"LAY",marketId,runnerId)				
		} yield placeLayBetEvent

		/**Create back bet events for delta between the new and the previous runner states.*/
		val backBetEvents:List[String] = for {
			deltaRunnerPrice <- marketRunnerDelta 
			if(deltaRunnerPrice.totalToLay != 0) 
				val placeBackBetEvent = if(deltaRunnerPrice.totalToLay >0)
					"""{"eventType":"PLACE_BET","userId":%s,"betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(userId,deltaRunnerPrice.totalToLay,deltaRunnerPrice.price,"BACK",marketId,runnerId)	
					else 
						"""{"eventType":"CANCEL_BETS","userId":%s,"betsSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(userId,-deltaRunnerPrice.totalToLay,deltaRunnerPrice.price,"BACK",marketId,runnerId)				

		} yield placeBackBetEvent

		layBetEvents ::: backBetEvents
	}

	/**Combines delta for runner prices with delta for traded volume and represents it as runner prices.
	 * 
	 * @param  runnerPricesDelta
	 * @param runnerTradedVolumeDelta
	 * 
	 * Example:
	 * runner price[price,toBack,toLay] = 1.9,2,0
	 * traded volume [price,volume] = 1.9,5
	 * runner price + traded volume = [price,toBack+volume,toLay+volume] = 1.9,7,5
	 * */
	def combine(runnerPricesDelta:List[IRunnerPrice], runnerTradedVolumeDelta:List[IPriceTradedVolume]):List[IRunnerPrice] = {
		val allPrices = (runnerPricesDelta.map(_.price) :::runnerTradedVolumeDelta.map(_.price)).distinct

		/**Total delta represents both runnerPricesDelta and tradedVolumeDelta in a form of runner prices.*/
		val totalDelta = for {
			price <- allPrices
			val deltaRunnerPrice = runnerPricesDelta.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val deltaTradedVolume = runnerTradedVolumeDelta.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val totalRunnerPriceDelta = new RunnerPrice(deltaRunnerPrice.price,deltaTradedVolume.totalMatchedAmount + deltaRunnerPrice.totalToBack,deltaTradedVolume.totalMatchedAmount + deltaRunnerPrice.totalToLay)
		} yield totalRunnerPriceDelta

		totalDelta
	}
	/**Calculates delta between the new and the previous state of the runner prices.
	 * 
	 * @param newRunnerPrices
	 * @param previousRunnerPrices
	 * @return Delta between the new and the previous state of the runner prices.
	 */
	def calculateRunnerPricesDelta(newRunnerPrices:List[IRunnerPrice],previousRunnerPrices:List[IRunnerPrice]):List[IRunnerPrice] = {

		val allPrices = (newRunnerPrices.map(_.price) :::previousRunnerPrices.map(_.price)).distinct

		/**Get delta between new and previous market prices.*/
		val deltaForRunnerPrices = for {
			price <- allPrices
			val newRunnerPrice = newRunnerPrices.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val previousRunnerPrice = previousRunnerPrices.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val runnerPriceDelta = new RunnerPrice(newRunnerPrice.price,newRunnerPrice.totalToBack-previousRunnerPrice.totalToBack,newRunnerPrice.totalToLay-previousRunnerPrice.totalToLay)
			if(runnerPriceDelta.totalToBack != 0 || runnerPriceDelta.totalToLay !=0)
		} yield runnerPriceDelta

		deltaForRunnerPrices
	}

	/**Calculates delta between the new and the previous state of the runner traded volume.
	 * 
	 * @param newTradedVolumes
	 * @param previousTradedVolumes
	 * @return Delta between the new and the previous state of the runner traded volume.
	 */
	def calculateTradedVolumeDelta(newTradedVolumes:List[IPriceTradedVolume],previousTradedVolumes:List[IPriceTradedVolume]):List[IPriceTradedVolume] = {
		val allPrices = (newTradedVolumes.map(_.price) :::previousTradedVolumes.map(_.price)).distinct

		/**Get delta between new and previous prices traded volume.*/
		val deltaForUpdatedAndNewTradedVolume = for {
			price <- allPrices
			val newTradedVolume = newTradedVolumes.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val previousTradedVolume = previousTradedVolumes.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val tradedVolumeDelta = new PriceTradedVolume(newTradedVolume.price,newTradedVolume.totalMatchedAmount-previousTradedVolume.totalMatchedAmount)
			if(tradedVolumeDelta.totalMatchedAmount != 0)
		} yield tradedVolumeDelta

		deltaForUpdatedAndNewTradedVolume
	}
}