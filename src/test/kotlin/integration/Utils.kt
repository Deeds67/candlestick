package integration

import Quote
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import repositories.InstrumentTable
import repositories.QuoteTable

object Utils {
    fun clearDatabase() = transaction {
        InstrumentTable.deleteAll()
        QuoteTable.deleteAll()
    }

    fun getAllQuotes() = transaction {
        QuoteTable.selectAll().map {
            Quote(
                isin = ISIN.create(it[QuoteTable.isin]),
                price = it[QuoteTable.price]
            )
        }
    }
}