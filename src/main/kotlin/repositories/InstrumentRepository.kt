package repositories

import ISIN
import Instrument

interface InstrumentRepository {
    fun createInstrument(instrument: Instrument): Int
    fun deleteInstrument(isin: ISIN): Int
    fun instrumentExists(isin: ISIN): Boolean
}