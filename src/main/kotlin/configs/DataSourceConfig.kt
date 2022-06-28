package configs

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

data class DataSourceConfig(
    val url: String,
    val user: String,
    val password: String,
) {
    companion object {
        fun fromConfig(config: Config): DataSourceConfig {
            val dataSourceConfig = config.getConfig("datasource")
            return DataSourceConfig(
                url = dataSourceConfig.getString("url"),
                user = dataSourceConfig.getString("user"),
                password = dataSourceConfig.getString("password")
            )
        }
    }

    fun toHikariDataSource(): HikariDataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = url
        hikariConfig.username = user
        hikariConfig.password = password
        return HikariDataSource(hikariConfig)
    }
}