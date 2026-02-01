package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE

class MariaDBConnector: Connector() {

    override val type: DBType = DBType.MARIADB

    override fun configure() {
        super.configure()
        hikariConfig.jdbcUrl = INSTANCE.config.getString("database.connectionUrl")
        hikariConfig.username = INSTANCE.config.getString("database.username")
        hikariConfig.password = INSTANCE.config.getString("database.password")
        hikariConfig.driverClassName = "org.mariadb.jdbc.Driver"
    }

}