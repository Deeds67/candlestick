package unit

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import repositories.QuoteRepository
import services.CandlestickManagerImpl
import kotlin.test.assertEquals

class CandlestickManagerTest {

    @Test
    fun `ensure getCandlesticks returns empty list when called with a non existent ISIN`() {
        val quoteRepository = mockk<QuoteRepository>()
        every { quoteRepository.getCandlesticksBetween(any(), any(), any(), any()) } returns listOf()
        val candlestickManager = CandlestickManagerImpl(quoteRepository)

        assertEquals(listOf(), candlestickManager.getCandlesticks(ISIN.create("AB9999999999")))
    }
}