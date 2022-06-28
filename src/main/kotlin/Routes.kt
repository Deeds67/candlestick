import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import services.CandlestickManager

class Routes(private val candlestickManager: CandlestickManager) {
    fun getCandlesticks(req: Request): Response {
        val isin = ISIN.create(req.query("isin") ?: return Response(Status.BAD_REQUEST).body("{'reason': 'missing_isin'}"))

        val body = jackson.writeValueAsBytes(candlestickManager.getCandlesticks(isin))

        return Response(Status.OK).body(body.inputStream())
    }
}