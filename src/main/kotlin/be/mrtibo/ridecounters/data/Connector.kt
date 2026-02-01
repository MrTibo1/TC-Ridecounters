package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

abstract class Connector {

    abstract val type: DBType
    val hikariConfig = HikariConfig()
    private lateinit var dataSource: HikariDataSource

    fun getConfig(): HikariConfig {
        return hikariConfig
    }

    open fun configure() {
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hikariConfig.connectionTimeout = INSTANCE.config.getLong("database.connectionTimeout", 10000)
        hikariConfig.connectionTestQuery = "SELECT 1"
    }

    fun open() {
        configure()
        dataSource = HikariDataSource(hikariConfig)
    }

    fun close() {
        dataSource.close()
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

}