package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import java.io.File

class SQLiteConnector: Connector() {

    override val type: DBType = DBType.SQLITE

    override fun configure() {
        super.configure()
        hikariConfig.jdbcUrl = "jdbc:${type.jdbcId}:${INSTANCE.dataFolder}${File.separator}${INSTANCE.config.getString("database.path", "ridecounter_data.db")}"
    }
}