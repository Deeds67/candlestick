package integration

import Quote
import com.typesafe.config.ConfigFactory
import configs.DataSourceConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import repositories.InstrumentTable
import repositories.QuoteTable

object Utils {
    private val config = ConfigFactory.load()
    private val dataSource = DataSourceConfig.fromConfig(config).toHikariDataSource()

    init {
        Database.connect(dataSource)
    }

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