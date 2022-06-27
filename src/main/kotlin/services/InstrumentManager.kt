package services

import InstrumentEvent
import repositories.InstrumentRepository

interface InstrumentManager {
    suspend fun processInstrumentEvent(instrumentEvent: InstrumentEvent)
}

class InstrumentManagerImpl(private val instrumentRepository: InstrumentRepository): InstrumentManager {
    override suspend fun processInstrumentEvent(instrumentEvent: InstrumentEvent) {
        when (instrumentEvent.type) {
            InstrumentEvent.Type.ADD -> instrumentRepository.createInstrument(instrumentEvent.data)
            InstrumentEvent.Type.DELETE -> instrumentRepository.deleteInstrument(instrumentEvent.data.isin)
        }
    }
}