import repositories.InstrumentRepository
import repositories.QuoteRepository
import java.math.BigDecimal
import java.time.Instant
import kotlin.random.Random

object Generators {
    private fun getRandomUppercaseString(length: Int): String {
        val allowedChars = ('A'..'Z')
        return generateRandomString(allowedChars, length)
    }

    private fun getRandomNumberString(length: Int): String {
        val allowedChars = ('0'..'9')
        return generateRandomString(allowedChars, length)
    }

    private fun generateRandomString(allowedChars: CharRange, length: Int): String =
        (1..length)
            .map { allowedChars.random() }
            .joinToString("")

    fun generateISIN(): ISIN = ISIN.create(getRandomUppercaseString(2) + getRandomNumberString(10))

    fun generateInstrument(instrumentRepository: InstrumentRepository): Instrument {
        val instrument = Instrument(generateISIN(), getRandomUppercaseString(Random.nextInt(0, 100)))
        instrumentRepository.createInstrument(instrument)
        return instrument
    }

    fun generateAndPopulateRandomInstrumentsWithQuotes(quoteRepository: QuoteRepository, instrumentRepository: InstrumentRepository) {
        (0..10).forEach {
            val instrument = generateInstrument(instrumentRepository)
            (0..20).forEach {
                quoteRepository.createQuote(
                    Quote(
                        instrument.isin,
                        BigDecimal("${Random.nextInt(0, 50000)}.${Random.nextInt(1000, 9999)}")
                    ),
                    Instant.now().minusSeconds(Random.nextLong(0, 3600))
                )
            }
        }
    }
}