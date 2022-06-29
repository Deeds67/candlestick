package integration

import Candlestick
import Instrument
import Quote
import com.typesafe.config.ConfigFactory
import configs.DataSourceConfig
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.InstrumentRepositoryImpl
import repositories.QuoteRepositoryImpl
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails

class QuoteRepositoryTest {
    private val config = ConfigFactory.load()
    private val dataSource = DataSourceConfig.fromConfig(config).toHikariDataSource()
    private val quoteRepository = QuoteRepositoryImpl()
    private val instrumentRepository = InstrumentRepositoryImpl()

    init {
        Database.connect(dataSource)
    }

    @BeforeEach
    fun beforeEach() {
        Utils.clearDatabase()
    }

    private fun createInstrument(): Instrument {
        val instrument = Instrument(ISIN.create("AB2222222222"), "Fake instrument")
        instrumentRepository.createInstrument(instrument)
        return instrument
    }

    @Test
    fun `ensure quotes are correctly inserted when the instrument exists`() {
        val instrument = createInstrument()

        val quotes = listOf(
            Quote(instrument.isin, BigDecimal("100.1234")),
            Quote(instrument.isin, BigDecimal("200.2222")),
            Quote(instrument.isin, BigDecimal("50"))
        )
        quotes.map { q ->
            assertEquals(1, quoteRepository.createQuote(q, Instant.now()))
        }

        val insertedQuotes = Utils.getAllQuotes().map { it.copy(price = it.price.stripTrailingZeros()) }
        assertEquals(3, insertedQuotes.size)

        quotes.map { q ->
            val withoutTrailingZeroes = q.copy(price = q.price.stripTrailingZeros())
            assertContains(insertedQuotes, withoutTrailingZeroes)
        }
    }

    @Test
    fun `ensure quotes are not inserted when corresponding instrument does not exist`() {
        val quote = Quote(ISIN.create("BB3333333333"), BigDecimal("123.12"))
        assertFails { quoteRepository.createQuote(quote, Instant.now()) }
    }

    @Test
    fun `ensure cascading delete of quotes when instrument is deleted`() {
        val instrument = createInstrument()
        val quote = Quote(instrument.isin, BigDecimal("123.12"))

        assertEquals(1, quoteRepository.createQuote(quote, Instant.now()))
        assertEquals(1, Utils.getAllQuotes().size)

        instrumentRepository.deleteInstrument(instrument.isin)
        assertEquals(0, Utils.getAllQuotes().size)
    }

    @Test
    fun `ensure candlestick history is returned from the date of the first quote`() {
        val instrument = createInstrument()
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("5.1234")), Instant.parse("2022-06-28T10:07:00Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("8.1231")), Instant.parse("2022-06-28T10:07:05Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("3.2222")), Instant.parse("2022-06-28T10:07:59Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("7.3454")), Instant.parse("2022-06-28T10:09:05Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("4.1111")), Instant.parse("2022-06-28T10:09:59Z"))

        val from = Instant.parse("2022-06-28T10:00:00Z")
        val to = from.plusSeconds(60 * 11) // 11 minutes
        val backfillUntil = to.minus(2, ChronoUnit.DAYS)

        val candlesticks = quoteRepository.getCandlesticksBetween(instrument.isin, from, to, backfillUntil)

        // Even though we ask for candlesticks from 10:00, we don't have any data until 10:07, so our candlesticks only start at 10:07, giving 4 candlesticks.
        assertEquals(4, candlesticks.size)
        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:07:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:08:00Z"),
                openPrice = BigDecimal("5.1234"),
                closingPrice = BigDecimal("3.2222"),
                lowPrice = BigDecimal("3.2222"),
                highPrice = BigDecimal("8.1231")
            ),
            candlesticks[0]
        )

        // There is no data between 08 and 09, so we just reuse the closing price from the previous minute for all of the values.
        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:08:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:09:00Z"),
                openPrice = BigDecimal("3.2222"),
                closingPrice = BigDecimal("3.2222"),
                lowPrice = BigDecimal("3.2222"),
                highPrice = BigDecimal("3.2222")
            ),
            candlesticks[1]
        )

        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:09:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:10:00Z"),
                openPrice = BigDecimal("7.3454"),
                closingPrice = BigDecimal("4.1111"),
                lowPrice = BigDecimal("4.1111"),
                highPrice = BigDecimal("7.3454")
            ),
            candlesticks[2]
        )
        // There is no data between 10 and 11, so we just reuse the closing price from the previous minute for all of the values.
        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:10:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:11:00Z"),
                openPrice = BigDecimal("4.1111"),
                closingPrice = BigDecimal("4.1111"),
                lowPrice = BigDecimal("4.1111"),
                highPrice = BigDecimal("4.1111")
            ),
            candlesticks[3]
        )
    }

    @Test
    fun `ensure candlestick is backfilled with the newest value if last known data is outside of the query range but within the backfill range`() {
        val instrument = createInstrument()
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("3.8567")), Instant.parse("2022-06-28T05:00:00Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("1.1234")), Instant.parse("2022-06-28T05:05:00Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("5.1234")), Instant.parse("2022-06-28T10:01:05Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("7.3333")), Instant.parse("2022-06-28T10:01:07Z"))
        quoteRepository.createQuote(Quote(instrument.isin, BigDecimal("2.3333")), Instant.parse("2022-06-28T10:01:09Z"))

        val from = Instant.parse("2022-06-28T10:00:00Z")
        val to = from.plusSeconds(60 * 3)
        val backfillUntil = to.minus(2, ChronoUnit.DAYS)

        val candlesticks = quoteRepository.getCandlesticksBetween(instrument.isin, from, to, backfillUntil)

        assertEquals(3, candlesticks.size)
        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:00:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:01:00Z"),
                openPrice = BigDecimal("1.1234"),
                closingPrice = BigDecimal("1.1234"),
                lowPrice = BigDecimal("1.1234"),
                highPrice = BigDecimal("1.1234")
            ),
            candlesticks[0]
        )
        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:01:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:02:00Z"),
                openPrice = BigDecimal("5.1234"),
                closingPrice = BigDecimal("2.3333"),
                lowPrice = BigDecimal("2.3333"),
                highPrice = BigDecimal("7.3333")
            ),
            candlesticks[1]
        )
        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:02:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:03:00Z"),
                openPrice = BigDecimal("2.3333"),
                closingPrice = BigDecimal("2.3333"),
                lowPrice = BigDecimal("2.3333"),
                highPrice = BigDecimal("2.3333")
            ),
            candlesticks[2]
        )
    }

    @Test
    fun `ensure the latest candlesticks closing time is not in the future`() {
        val instrument = createInstrument()
        // Hardcoding the clock to return 2022-06-28T10:01:45Z when `Instant.now()` is called
        val fixedClock = Clock.fixed(Instant.parse("2022-06-28T10:01:45Z"), ZoneId.of("UTC"))
        val quoteRepositoryWithFixedClock = QuoteRepositoryImpl(fixedClock)
        quoteRepositoryWithFixedClock.createQuote(Quote(instrument.isin, BigDecimal("3.8567")), Instant.parse("2022-06-28T05:00:00Z"))
        quoteRepositoryWithFixedClock.createQuote(Quote(instrument.isin, BigDecimal("5.1234")), Instant.parse("2022-06-28T10:01:05Z"))

        val from = Instant.parse("2022-06-28T10:00:00Z")
        val to = from.plusSeconds(60 * 2)
        val backfillUntil = to.minus(2, ChronoUnit.DAYS)

        val candlesticks = quoteRepositoryWithFixedClock.getCandlesticksBetween(instrument.isin, from, to, backfillUntil)

        assertEquals(2, candlesticks.size)

        assertEquals(
            Candlestick(
                openTimestamp = Instant.parse("2022-06-28T10:01:00Z"),
                closeTimestamp = Instant.parse("2022-06-28T10:01:45Z"), // Makes sure that the closing timestamp is not in the future
                openPrice = BigDecimal("5.1234"),
                closingPrice = BigDecimal("5.1234"),
                lowPrice = BigDecimal("5.1234"),
                highPrice = BigDecimal("5.1234")
            ),
            candlesticks[1]
        )
    }
}