package services

import QuoteEvent
import repositories.InstrumentRepository
import repositories.QuoteRepository
import java.time.Instant

interface QuoteManager {
    fun processQuoteEvent(quoteEvent: QuoteEvent)
}

class QuoteManagerImpl(
    private val quoteRepository: QuoteRepository,
    private val instrumentRepository: InstrumentRepository
) : QuoteManager {
    override fun processQuoteEvent(quoteEvent: QuoteEvent) {
        val instrumentExists = instrumentRepository.instrumentExists(quoteEvent.data.isin)
        if (instrumentExists) {
            quoteRepository.createQuote(quoteEvent.data, Instant.now())
        }
    }
}