package services

import Candlestick
import ISIN
import repositories.QuoteRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

interface CandlestickManager {
    fun getCandlesticks(isin: ISIN): List<Candlestick>
}

class CandlestickManagerImpl(private val quoteRepository: QuoteRepository): CandlestickManager {
    override fun getCandlesticks(isin: ISIN): List<Candlestick> {
        val to = Instant.now()
        val from = to.minus(30, ChronoUnit.MINUTES)
        val backfillUntil = to.minus(2, ChronoUnit.DAYS)
        return quoteRepository.getCandlesticksBetween(isin, from, to, backfillUntil)
    }
}