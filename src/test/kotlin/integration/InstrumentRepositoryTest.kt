package integration

import Generators.generateISIN
import Instrument
import Quote
import com.typesafe.config.ConfigFactory
import configs.DataSourceConfig
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.InstrumentRepositoryImpl
import java.math.BigDecimal
import kotlin.test.assertEquals

class InstrumentRepositoryTest {
    private val instrumentRepository = InstrumentRepositoryImpl()

    @BeforeEach
    fun beforeEach() {
        Utils.clearDatabase()
    }

    @Test
    fun `ensure CRUD operations on instruments function correctly`() {
        val instrument = Instrument(generateISIN(), "Test description")
        assertEquals(1, instrumentRepository.createInstrument(instrument))
        assertEquals(0, instrumentRepository.createInstrument(instrument))

        assertEquals(true, instrumentRepository.instrumentExists(instrument.isin))

        assertEquals(1, instrumentRepository.deleteInstrument(instrument.isin))
        assertEquals(false, instrumentRepository.instrumentExists(instrument.isin))
        assertEquals(0, instrumentRepository.deleteInstrument(instrument.isin))
    }
}