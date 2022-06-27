package unit

import Instrument
import InstrumentEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import repositories.InstrumentRepository
import services.InstrumentManagerImpl

class InstrumentManagerTest {
    private val mockInstrumentRepository = mockk<InstrumentRepository>(relaxed = true)
    private val instrumentManager = InstrumentManagerImpl(mockInstrumentRepository)

    @Test
    fun `ensure instrument is persisted when ADD event is received`() =
        runBlockingTest {
            val instrumentAddEvent = InstrumentEvent(
                type = InstrumentEvent.Type.ADD,
                data = Instrument("AB1234567890", "Fake instrument")
            )

            instrumentManager.processInstrumentEvent(instrumentAddEvent)

            verify { mockInstrumentRepository.createInstrument(instrumentAddEvent.data) }
        }

    @Test
    fun `ensure instrument is deleted when DELETE event is received`() =
        runBlockingTest {
            val mockInstrumentRepository = mockk<InstrumentRepository>(relaxed = true)
            val instrumentAddEvent = InstrumentEvent(
                type = InstrumentEvent.Type.DELETE,
                data = Instrument("AB1234567890", "Fake instrument")
            )
            val instrumentManager = InstrumentManagerImpl(mockInstrumentRepository)

            instrumentManager.processInstrumentEvent(instrumentAddEvent)
            
            verify { mockInstrumentRepository.deleteInstrument(instrumentAddEvent.data.isin) }
        }
}