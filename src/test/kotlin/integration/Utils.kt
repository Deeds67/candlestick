package integration

import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import repositories.InstrumentsTable

object Utils {
    fun clearDatabase() = transaction {
        InstrumentsTable.deleteAll()
//        QuotesTable.deleteAll()
    }
}