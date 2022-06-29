package integration

import Quote
import com.typesafe.config.ConfigFactory
import configs.DataSourceConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import repositories.InstrumentTable
import repositories.QuoteTable

object DBUtils {
    val config = ConfigFactory.load()
    val dataSourceConfig = DataSourceConfig.fromConfig(config)

    val flyway = Flyway.configure().dataSource(dataSourceConfig.url, dataSourceConfig.user, dataSourceConfig.password).load()


    init {
        flyway.migrate()
        val dataSource by lazy{ dataSourceConfig.toHikariDataSource() }
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