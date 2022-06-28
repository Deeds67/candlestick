package repositories

import Candlestick
import ISIN
import Quote
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface QuoteRepository {
    fun createQuote(quote: Quote): Int
    fun getCandlesticksBetween(isin: ISIN, from: Instant, to: Instant): List<Candlestick>
}

object QuoteTable : Table("data.quotes") {
    val isin = text("isin")
    val price = decimal("price", 15, 4)
    val created_at = timestamp("created_at").clientDefault { Instant.now() }
}

class QuoteRepositoryImpl: QuoteRepository {
    override fun createQuote(quote: Quote): Int =
        transaction {
            QuoteTable.insert {
                it[isin] = quote.isin.value
                it[price] = quote.price
            }.insertedCount
        }

    override fun getCandlesticksBetween(isin: ISIN, from: Instant, to: Instant): List<Candlestick> {
        TODO("Not yet implemented")
    }
}