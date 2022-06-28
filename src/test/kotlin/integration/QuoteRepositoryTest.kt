package integration

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
            assertEquals(1, quoteRepository.createQuote(q))
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
        assertFails { quoteRepository.createQuote(quote) }
    }

    @Test
    fun `ensure cascading delete of quotes when instrument is deleted`() {
        val instrument = createInstrument()
        val quote = Quote(instrument.isin, BigDecimal("123.12"))

        assertEquals(1, quoteRepository.createQuote(quote))
        assertEquals(1, Utils.getAllQuotes().size)

        instrumentRepository.deleteInstrument(instrument.isin)
        assertEquals(0, Utils.getAllQuotes().size)
    }
}