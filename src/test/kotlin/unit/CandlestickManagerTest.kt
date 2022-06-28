package unit

import org.junit.jupiter.api.Test
import services.CandlestickManagerImpl
import kotlin.test.assertEquals

class CandlestickManagerTest {

    @Test
    fun `ensure getCandlesticks returns empty list when called with a non existent ISIN`() {
        val candlestickManager = CandlestickManagerImpl()

        assertEquals(candlestickManager.getCandlesticks("AB9999999999"), listOf())
    }
}