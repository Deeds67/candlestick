import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer

class Server(
  routeImplementations: Routes,
  port: Int = 9000,
) {
  private val routes = routes(
    "candlesticks" bind Method.GET to { routeImplementations.getCandlesticks(it) }
  )

  private val server: Http4kServer = routes.asServer(Netty(port))

  fun start() {
    server.start()
  }
}
