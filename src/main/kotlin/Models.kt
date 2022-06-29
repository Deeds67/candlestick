import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
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

data class ISIN private constructor(@get:JsonValue val value: String) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun create(v: String): ISIN {
            val regex = Regex("""^[A-Z]{2}([A-Z0-9]){9}[0-9]${'$'}""")
            return if (v matches regex) ISIN(v) else throw Exception("Invalid ISIN: $v")
        }
    }
}

data class Quote(val isin: ISIN, val price: Price)
typealias Price = BigDecimal

data class Candlestick(
    @JsonProperty("open_timestamp") val openTimestamp: Instant,
    @JsonProperty("close_timestamp") val closeTimestamp: Instant,
    @JsonProperty("open_price") val openPrice: Price,
    @JsonProperty("high_price") val highPrice: Price,
    @JsonProperty("low_price") val lowPrice: Price,
    @JsonProperty("closing_price") val closingPrice: Price
)
