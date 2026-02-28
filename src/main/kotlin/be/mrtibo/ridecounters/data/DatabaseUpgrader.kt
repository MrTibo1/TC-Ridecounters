package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.moveTo

object DatabaseUpgrader {

    private fun hasLegacyFormat(): Boolean {
        Database.connection().use { conn ->
            val stmt = conn.prepareStatement(when (Database.type) {
                DBType.SQLITE -> """
                    SELECT name FROM sqlite_schema WHERE 
                    type ='table' AND
                    name LIKE 'ridecounter_data';
                """.trimIndent()
                DBType.MARIADB -> """SHOW TABLES LIKE 'ridecounter_data';"""
            })

            return stmt.executeQuery().next()
        }
    }

    fun copySqliteDB() {
        val oldPluginPath = INSTANCE.dataPath.parent.resolve("Ridecounters")
        val oldConfigFile = oldPluginPath.resolve("config.yml").toFile()
        if (!oldConfigFile.exists()) return

        val oldSqliteName = YamlConfiguration.loadConfiguration(oldConfigFile).getString("database.file") ?: "ridecounters.db"
        val sqliteFile = oldPluginPath.resolve(oldSqliteName)

        if (!sqliteFile.exists()) return
        INSTANCE.logger.info("Copying legacy format sqlite database...")
        sqliteFile.copyTo(INSTANCE.dataPath.resolve(INSTANCE.config.getString("database.path") ?: "ridecounter_data.db"))
        sqliteFile.moveTo(oldPluginPath.resolve("$oldSqliteName.old"))
    }

    fun attemptUpgrade() {
        if (!hasLegacyFormat()) return
        INSTANCE.logger.info("Found legacy ridecounter data, attempting to upgrade...")

        Database.connection().use { conn ->
            val stmt = conn.createStatement()

            stmt.addBatch("""
                INSERT INTO players(uuid, username)
                SELECT uuid, lastName FROM ridecounter_players;
            """.trimIndent())

            stmt.addBatch("""
                INSERT INTO rides(id, name, altname)
                SELECT id, name, shortName FROM ridecounter_rides;
            """.trimIndent())

            stmt.addBatch("""
                INSERT INTO ridecount_total(ride_id, player_uuid, total)
                SELECT d.ride, p.uuid, d.val FROM ridecounter_data d
                INNER JOIN ridecounter_players p ON d.player = p.id;
            """.trimIndent())

            stmt.executeBatch()
        }

        // Rename legacy tables, so updating will only occur once.
        finishUpgrade()

        INSTANCE.logger.info("Finished upgrading legacy data.")
    }

    private fun finishUpgrade() {
        INSTANCE.logger.info("Renaming legacy tables...")
        Database.connection().use { conn ->
            val statement = conn.createStatement()
            statement.addBatch("""ALTER TABLE ridecounter_players RENAME TO ridecounter_players_old;""")
            statement.addBatch("""ALTER TABLE ridecounter_rides RENAME TO ridecounter_rides_old;""")
            statement.addBatch("""ALTER TABLE ridecounter_data RENAME TO ridecounter_data_old;""")
            statement.executeBatch()
        }
    }

}