package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE

class SQLiteConnector: Connector() {

    override val type: DBType = DBType.SQLITE

    override fun configure() {
        super.configure()
        hikariConfig.jdbcUrl = "jdbc:${type.jdbcId}:${INSTANCE.dataFolder}\\${INSTANCE.config.getString("database.path", "ridecounter_data.db")}"
    }
}