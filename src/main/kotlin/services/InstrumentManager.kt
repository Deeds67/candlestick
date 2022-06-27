package services

import InstrumentEvent
import repositories.InstrumentRepository

interface InstrumentManager {
    suspend fun processInstrumentEvent(instrumentEvent: InstrumentEvent)
}

class InstrumentManagerImpl(instrumentRepository: InstrumentRepository): InstrumentManager {
    override suspend fun processInstrumentEvent(instrumentEvent: InstrumentEvent) {
        TODO("Not yet implemented")
    }
}