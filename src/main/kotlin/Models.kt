import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.Instant

data class InstrumentEvent(val type: Type, val data: Instrument) {
    enum class Type {
        ADD,
        DELETE
    }
}

data class QuoteEvent(val data: Quote)

data class Instrument(val isin: ISIN, val description: String)
typealias ISIN = String

data class Quote(val isin: ISIN, val price: Price)
typealias Price = BigDecimal

data class Candlestick(
    @JsonProperty("open_timestamp") val openTimestamp: Instant,
    @JsonProperty("close_timestamp") var closeTimestamp: Instant,
    @JsonProperty("open_price") val openPrice: Price,
    @JsonProperty("high_price") var highPrice: Price,
    @JsonProperty("low_price") var lowPrice: Price,
    @JsonProperty("closing_price") var closingPrice: Price
)