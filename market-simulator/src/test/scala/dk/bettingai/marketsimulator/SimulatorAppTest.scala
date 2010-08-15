package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import java.io._ 
import java.util.zip._
class SimulatorAppTest {

	/** Check against exceptions only.
	 * 
	 */
	@Test def test = SimulatorApp.main(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader"))

	@Test def testWrongInputParameters() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("incorrect parameters"),new PrintStream(consoleStream))

		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Market Simulator Copyright 2010 Daniel Korzekwa(http://danmachine.com)"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Usage"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("java.lang.IllegalArgumentException: requirement failed: The marketData and traderImpl arguments must be provided."))
	}

	@Test def testCorrectInputParametersEmptyEventData() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader"),new PrintStream(consoleStream))

		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertFalse("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("expProfit"))
	}

	@Test def testSimpleTraderAndPlaceBetEvent() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataPlaceAndCancelLayBet.csv","traderImpl=dk.bettingai.marketsimulator.trader.SimpleTrader"),new PrintStream(consoleStream))

		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Match Odds: Man Utd vs Arsenal expProfit=-0.61 expAggrProfit=-0.61 mBets=3 uBets=1"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("TotalExpectedProfit=-0.61 TotalMatchedBets=3 TotalUnmachedBets=1"))

	}

	@Test def testSimpleTraderAndBetsForTwoMarkets() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataPlaceBackBetOnTwoMarkets.csv","traderImpl=dk.bettingai.marketsimulator.trader.SimpleTrader"),new PrintStream(consoleStream))
		println(new String(consoleStream.toByteArray))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Match Odds: Fulham vs Wigan expProfit=0.6 expAggrProfit=0.6 mBets=1 uBets=1"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Match Odds: Man Utd vs Arsenal expProfit=1.06 expAggrProfit=1.66 mBets=2 uBets=2"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("TotalExpectedProfit=1.66 TotalMatchedBets=3 TotalUnmachedBets=3"))

	}
}


