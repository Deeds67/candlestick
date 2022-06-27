package unit

import Quote
import QuoteEvent
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import repositories.InstrumentRepository
import repositories.QuoteRepository
import services.QuoteManagerImpl
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class QuoteManagerTest {
    @Test
    fun `ensure quote is inserted if the corresponding instrument exists`(
        @MockK mockInstrumentRepository: InstrumentRepository,
        @RelaxedMockK mockQuoteRepository: QuoteRepository
    ) = runBlockingTest {
        val quoteManager = QuoteManagerImpl(mockQuoteRepository, mockInstrumentRepository)
        val quoteEvent = QuoteEvent(Quote("AB1234567890", BigDecimal("100.232")))

        every { mockInstrumentRepository.instrumentExists(any()) } returns true

        quoteManager.processQuoteEvent(quoteEvent)

        verify { mockInstrumentRepository.instrumentExists(quoteEvent.data.isin) }
        verify { mockQuoteRepository.createQuote(quoteEvent.data) }
    }

    @Test
    fun `ensure quote is not inserted if corresponding instrument does not exist`(
        @MockK mockInstrumentRepository: InstrumentRepository,
        @RelaxedMockK mockQuoteRepository: QuoteRepository
    ) = runBlockingTest {
        val quoteManager = QuoteManagerImpl(mockQuoteRepository, mockInstrumentRepository)
        val quoteEvent = QuoteEvent(Quote("AB1234567890", BigDecimal("100.232")))

        every { mockInstrumentRepository.instrumentExists(any()) } returns false

        quoteManager.processQuoteEvent(quoteEvent)

        verify { mockInstrumentRepository.instrumentExists(quoteEvent.data.isin) }
        verify(exactly = 0) { mockQuoteRepository.createQuote(quoteEvent.data) }
    }
}