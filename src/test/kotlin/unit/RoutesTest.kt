package unit

import Candlestick
import Generators.generateISIN
import Routes
import com.natpryce.hamkrest.assertion.assertThat
import io.mockk.every
import io.mockk.mockk
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasStatus
import services.CandlestickManager
import java.math.BigDecimal
import java.time.Instant
import com.natpryce.hamkrest.and
import org.http4k.hamkrest.hasBody
import org.junit.jupiter.api.Test

class RoutesTest {

    @Test
    fun `ensure GET candlesticks returns properly serialized response`() {
        val mockCandlestickManager = mockk<CandlestickManager>()
        val mockCandlesticks = listOf(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-27T17:29:00Z"),
                closeTimestamp = Instant.parse("2022-06-27T17:30:00Z"),
                openPrice = BigDecimal("243.5856"),
                lowPrice = BigDecimal("235.1712"),
                highPrice = BigDecimal("243.9369"),
                closingPrice = BigDecimal("235.1712")
            ),
            Candlestick(
                openTimestamp = Instant.parse("2022-06-27T17:30:00Z"),
                closeTimestamp = Instant.parse("2022-06-27T17:31:00Z"),
                openPrice = BigDecimal("243.5856"),
                lowPrice = BigDecimal("222.222"),
                highPrice = BigDecimal("243.9369"),
                closingPrice = BigDecimal("222.1122")
            )
        )
        every { mockCandlestickManager.getCandlesticks(any()) } returns mockCandlesticks
        val routes = Routes(mockCandlestickManager)

        val response = routes.getCandlesticks(Request(Method.GET, "/candlesticks").query("isin", generateISIN().value))

        assertThat(
            response,
            hasStatus(Status.OK).and(hasBody("""{"data":[{"open_timestamp":"2022-06-27T17:29:00Z","close_timestamp":"2022-06-27T17:30:00Z","open_price":243.5856,"high_price":243.9369,"low_price":235.1712,"closing_price":235.1712},{"open_timestamp":"2022-06-27T17:30:00Z","close_timestamp":"2022-06-27T17:31:00Z","open_price":243.5856,"high_price":243.9369,"low_price":222.222,"closing_price":222.1122}]}"""))
        )
    }

    @Test
    fun `ensure GET candlesticks returns bad request when no isin is specified`() {
        val mockCandlestickManager = mockk<CandlestickManager>()
        val routes = Routes(mockCandlestickManager)
        val response = routes.getCandlesticks(Request(Method.GET, "/candlesticks"))

        assertThat(response, hasStatus(Status.BAD_REQUEST).and(hasBody("{'reason': 'missing_isin'}")))
    }

    @Test
    fun `ensure GET candlesticks returns 404 if no data is found for this time period`() {
        val mockCandlestickManager = mockk<CandlestickManager>()
        every { mockCandlestickManager.getCandlesticks(any()) } returns listOf()
        val routes = Routes(mockCandlestickManager)
        val response = routes.getCandlesticks(Request(Method.GET, "/candlesticks").query("isin", generateISIN().value))

        assertThat(response, hasStatus(Status.NOT_FOUND).and(hasBody("{'reason': 'no_data_for_isin'}")))
    }
}