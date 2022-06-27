package services

import QuoteEvent
import repositories.InstrumentRepository
import repositories.QuoteRepository

interface QuoteManager {
    suspend fun processQuoteEvent(quoteEvent: QuoteEvent)
}

class QuoteManagerImpl(
    private val quoteRepository: QuoteRepository,
    private val instrumentRepository: InstrumentRepository
) : QuoteManager {
    override suspend fun processQuoteEvent(quoteEvent: QuoteEvent) {
        val instrumentExists = instrumentRepository.instrumentExists(quoteEvent.data.isin)
        if (instrumentExists) {
            quoteRepository.createQuote(quoteEvent.data)
        }
    }
}