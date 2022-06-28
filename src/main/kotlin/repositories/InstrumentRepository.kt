package repositories

import ISIN
import Instrument
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface InstrumentRepository {
    fun createInstrument(instrument: Instrument): Int
    fun deleteInstrument(isin: ISIN): Int
    fun instrumentExists(isin: ISIN): Boolean
}

object InstrumentTable : Table("data.instruments") {
    val isin = text("isin")
    val description = text("description").nullable()
    val created_at = timestamp("created_at").clientDefault { Instant.now() }
}

class InstrumentRepositoryImpl: InstrumentRepository {
    override fun createInstrument(instrument: Instrument): Int = transaction {
        InstrumentTable.insertIgnore {
            it[isin] = instrument.isin.value
            it[description] = instrument.description
        }.insertedCount
    }

    override fun deleteInstrument(isin: ISIN): Int = transaction {
        InstrumentTable.deleteWhere {
            InstrumentTable.isin eq isin.value
        }
    }

    override fun instrumentExists(isin: ISIN): Boolean =
        transaction {
            InstrumentTable.select {
                InstrumentTable.isin eq isin.value
            }.any()
        }

}