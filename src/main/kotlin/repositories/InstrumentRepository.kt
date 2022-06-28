package repositories

import ISIN
import Instrument
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

object InstrumentsTable : Table("data.instruments") {
    val isin = text("isin")
    val description = text("description").nullable()
    val created_at = timestamp("created_at").clientDefault { Instant.now() }
}

class InstrumentRepositoryImpl: InstrumentRepository {
    override fun createInstrument(instrument: Instrument): Int = transaction {
        InstrumentsTable.insertIgnore {
            it[isin] = instrument.isin
            it[description] = instrument.description
        }.insertedCount
    }

    override fun deleteInstrument(isin: ISIN): Int = transaction {
        InstrumentsTable.deleteWhere {
            InstrumentsTable.isin eq isin
        }
    }

    override fun instrumentExists(isin: ISIN): Boolean =
        transaction {
            InstrumentsTable.select {
                InstrumentsTable.isin eq isin
            }.any()
        }

}