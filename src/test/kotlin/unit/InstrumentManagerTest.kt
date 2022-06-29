@file:OptIn(ExperimentalCoroutinesApi::class)

package unit

import Generators.generateISIN
import Instrument
import InstrumentEvent
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import repositories.InstrumentRepository
import services.InstrumentManagerImpl

@ExtendWith(MockKExtension::class)
class InstrumentManagerTest {

    @Test
    fun `ensure instrument is persisted when ADD event is received`(@RelaxedMockK mockInstrumentRepository: InstrumentRepository) =
        runTest {
            val instrumentManager = InstrumentManagerImpl(mockInstrumentRepository)
            val instrumentAddEvent = InstrumentEvent(
                type = InstrumentEvent.Type.ADD,
                data = Instrument(generateISIN(), "Fake instrument")
            )

            instrumentManager.processInstrumentEvent(instrumentAddEvent)

            verify { mockInstrumentRepository.createInstrument(instrumentAddEvent.data) }
        }

    @Test
    fun `ensure instrument is deleted when DELETE event is received`(@RelaxedMockK mockInstrumentRepository: InstrumentRepository) =
        runTest {
            val instrumentManager = InstrumentManagerImpl(mockInstrumentRepository)
            val instrumentAddEvent = InstrumentEvent(
                type = InstrumentEvent.Type.DELETE,
                data = Instrument(generateISIN(), "Fake instrument")
            )

            instrumentManager.processInstrumentEvent(instrumentAddEvent)

            verify { mockInstrumentRepository.deleteInstrument(instrumentAddEvent.data.isin) }
        }
}