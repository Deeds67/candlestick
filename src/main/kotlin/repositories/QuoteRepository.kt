package repositories

import Candlestick
import ISIN
import Quote
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import repositories.DBUtils.execAndMap
import java.sql.ResultSet
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

interface QuoteRepository {
    fun createQuote(quote: Quote, createdAt: Instant): Int
    fun getCandlesticksBetween(
        isin: ISIN,
        from: Instant,
        to: Instant,
        backfillUntil: Instant
    ): List<Candlestick>
}

object QuoteTable : Table("data.quotes") {
    val isin = text("isin")
    val price = decimal("price", 15, 4)
    val created_at = timestamp("created_at")
}

class QuoteRepositoryImpl(private val clock: Clock = Clock.systemUTC()) : QuoteRepository {
    override fun createQuote(quote: Quote, createdAt: Instant): Int =
        transaction {
            QuoteTable.insert {
                it[isin] = quote.isin.value
                it[price] = quote.price
                it[created_at] = createdAt
            }.insertedCount
        }

    /**
     * Returns the candlesticks starting at [from] until [to].
     * In case there are missing candlesticks between [from] and [to], the previous known candlestick's `close_price` will be used.
     * In case the very first candlestick is missing, [backfillUntil] can be specified to query into the past to find the latest candlestick.
     * time_bucket_gapfill creates time buckets of 1 minute even if there is no row during this bucket: https://docs.timescale.com/api/latest/hyperfunctions/gapfilling-interpolation/time_bucket_gapfill/
     * locf = last observation carried forward. If the value is null, it uses the value from the last not null bucket: https://docs.timescale.com/api/latest/hyperfunctions/gapfilling-interpolation/locf/#locf
     */
    override fun getCandlesticksBetween(
        isin: ISIN,
        from: Instant,
        to: Instant,
        backfillUntil: Instant
    ): List<Candlestick> = transaction {
        """
            SELECT
                time_bucket_gapfill('1 minute', created_at) AS bucket,
                first(price,created_at) as maybe_open,
                locf(last(price, created_at), (select price FROM data.quotes q2 WHERE q2.created_at >= '$backfillUntil' and q2.created_at < '$from' and q2.isin = q.isin ORDER BY q2.created_at DESC LIMIT 1)) as close,
                max(price) as maybe_high,
                min(price) as maybe_low
            FROM data.quotes q
            WHERE created_at >= '$from' AND created_at < '$to'
            and isin = '${isin.value}'
            GROUP BY bucket, isin
        """.trimIndent().execAndMap { rs ->
            resultSetToMaybeCandlestick(rs)
        }.filterNotNull()
    }

    /**
     * Tries to convert the [rs] to a [Candlestick]. If the resultset does not contain the necessary fields, [null] is returned.
     * If the [maybeCloseTimestamp] is in the future, the candlestick's [closeTimestamp] is changed to be [Instant.now()]
     */
    private fun resultSetToMaybeCandlestick(rs: ResultSet): Candlestick? {
        val maybeOpenTimestamp = rs.getTimestamp("bucket")?.toInstant()
        val maybeOpenPrice = rs.getBigDecimal("maybe_open")
        val maybeCloseTimestamp =
            rs.getTimestamp("bucket")?.toInstant()?.plusSeconds(60)
        val maybeLowPrice = rs.getBigDecimal("maybe_low")
        val maybeHighPrice = rs.getBigDecimal("maybe_high")
        val maybeClosePrice = rs.getBigDecimal("close")

        return if (maybeOpenTimestamp != null && maybeCloseTimestamp != null && maybeClosePrice != null) {
            val isInFuture = maybeCloseTimestamp.isAfter(Instant.now(clock))

            Candlestick(
                openTimestamp = maybeOpenTimestamp,
                openPrice = maybeOpenPrice ?: maybeClosePrice,
                closeTimestamp = if (isInFuture) Instant.now(clock) else maybeCloseTimestamp,
                lowPrice = maybeLowPrice ?: maybeClosePrice,
                highPrice = maybeHighPrice ?: maybeClosePrice,
                closingPrice = maybeClosePrice
            )
        } else null
    }
}