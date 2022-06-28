import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import services.CandlestickManager

class Routes(private val candlestickManager: CandlestickManager) {
    fun getCandlesticks(req: Request): Response {
        val isin = ISIN.create(req.query("isin") ?: return Response(Status.BAD_REQUEST).body("{'reason': 'missing_isin'}"))

        val candlesticks = candlestickManager.getCandlesticks(isin)

        data class CandlesticksResponseBody(@JsonProperty("data") val data: List<Candlestick>)

        return if (candlesticks.isEmpty()) {
            Response(Status.NOT_FOUND).body("{'reason': 'no_data_for_isin'}")
        } else {
            val body = jackson.writeValueAsBytes(CandlesticksResponseBody(candlesticks))
            Response(Status.OK).body(body.inputStream())
        }
    }
}