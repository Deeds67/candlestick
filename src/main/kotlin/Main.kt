import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import configs.DataSourceConfig
import org.jetbrains.exposed.sql.Database
import repositories.InstrumentRepository
import repositories.InstrumentRepositoryImpl
import repositories.QuoteRepository
import repositories.QuoteRepositoryImpl
import services.*

fun main() {
    println("starting up")

    val instrumentRepository: InstrumentRepository = InstrumentRepositoryImpl()
    val quoteRepository: QuoteRepository = QuoteRepositoryImpl()

    val candlestickManager: CandlestickManager = CandlestickManagerImpl(quoteRepository)
    val instrumentManager: InstrumentManager = InstrumentManagerImpl(instrumentRepository)
    val quoteManager: QuoteManager = QuoteManagerImpl(quoteRepository, instrumentRepository)

    val routes = Routes(candlestickManager)
    val server = Server(routes)

    val instrumentStream = InstrumentStream()
    val quoteStream = QuoteStream()

    val config = ConfigFactory.load()
    val dataSource = DataSourceConfig.fromConfig(config).toHikariDataSource()

    Database.connect(dataSource)

    instrumentStream.connect { event ->
        instrumentManager.processInstrumentEvent(event)
        println(event)
    }

    quoteStream.connect { event ->
        quoteManager.processQuoteEvent(event)
        println(event)
    }

    server.start()
}

val jackson: ObjectMapper =
    jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
