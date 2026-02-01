package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters
import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.data.records.PlayerRecord
import be.mrtibo.ridecounters.data.records.RideRecord
import be.mrtibo.ridecounters.data.records.RidecountTopRecord
import be.mrtibo.ridecounters.data.records.RidecountTotalRecord
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

object Database {

    private lateinit var connector: Connector

    val type
        get() = connector.type

    fun setupConnection() {
        val type = INSTANCE.config.getString("database.type", "sqlite")
        connector = when (type?.lowercase()?.trim()) {
            "mariadb" -> MariaDBConnector()
            "sqlite" -> SQLiteConnector()
            else -> throw IllegalArgumentException("Unsupported database type '$type'")
        }
        connector.open()
    }

    fun shutdown() {
        try {
            connector.close()
        } catch (_: Throwable) { }
    }

    fun createTables() {
        val schemaPathName = "/schema/${connector.type.typeName}.sql"
        this.javaClass.getResourceAsStream(schemaPathName).use { inputStream ->
            if (inputStream == null) {
                INSTANCE.logger.severe("No schema found at $schemaPathName")
                return
            }
            val full = inputStream.bufferedReader().readLines()
            val statements = mutableListOf<String>()
            val sb = StringBuilder()
            full.forEach { line ->
                if (line.isNotBlank()) {
                    sb.append(line)
                    if (line.endsWith(");")) {
                        statements.add(sb.toString())
                        sb.clear()
                    }
                }
            }
            connection().use { connection ->
                val stmt = connection.createStatement()
                statements.forEach(stmt::addBatch)
                stmt.executeBatch()
            }
        }
    }

    private fun connection(): Connection = connector.getConnection()

    suspend fun createRide(id: String, name: String) = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            val stmt = it.prepareStatement("INSERT INTO rides (id, name) VALUES (?, ?)")
            stmt.setString(1, id)
            stmt.setString(2, name)
            stmt.executeUpdate()
        }
    }

    suspend fun deleteRide(id: String): Boolean = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            val stmt = it.prepareStatement("DELETE FROM rides WHERE id = ?")
            stmt.setString(1, id)
            val num = stmt.executeUpdate()
            return@withContext num > 0
        }
    }

    suspend fun setCounter(uuid: String, rideId: String, value: Int): RidecountTotalRecord? = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            val setStatement = when (connector.type) {
                DBType.MARIADB -> """
                    INSERT INTO ridecount_total (ride_id, player_uuid, total)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE total = ?
                """.trimIndent()
                DBType.SQLITE -> """
                    INSERT INTO ridecount_total (ride_id, player_uuid, total)
                    VALUES (?, ?, ?)
                    ON CONFLICT DO UPDATE SET total = ?
                """.trimIndent()
            }
            val stmt = it.prepareStatement(setStatement)
            stmt.setString(1, rideId)
            stmt.setString(2, uuid)
            stmt.setInt(3, value)
            stmt.setInt(4, value)

            stmt.executeUpdate()
        }
        return@withContext getTotalRidecount(uuid, rideId)
    }

    suspend fun incrementCounter(uuid: String, rideId: String): RidecountTotalRecord? = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            it.autoCommit = false
            val incrementStatement = when (connector.type) {
                DBType.MARIADB -> """
                    INSERT INTO ridecount_total (ride_id, player_uuid)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE total = total + 1
                """.trimIndent()

                DBType.SQLITE -> """
                    INSERT INTO ridecount_total (ride_id, player_uuid)
                    VALUES (?, ?)
                    ON CONFLICT DO UPDATE SET total = total + 1
                """.trimIndent()
            }
            val totalStmt = it.prepareStatement(incrementStatement)
            totalStmt.setString(1, rideId)
            totalStmt.setString(2, uuid)
            totalStmt.executeUpdate()

            val dataStmt = it.prepareStatement("""
                INSERT INTO ridecount_data (ride_id, player_uuid)
                VALUES (?, ?)
            """.trimIndent())
            dataStmt.setString(1, rideId)
            dataStmt.setString(2, uuid)
            dataStmt.executeUpdate()

            it.commit()

        }
        return@withContext getTotalRidecount(uuid, rideId)
    }

    suspend fun getTotalRidecount(uuid: String, rideId: String): RidecountTotalRecord? = withContext(Ridecounters.asyncDispatcher) {
        val record: RidecountTotalRecord
        connection().use {
            val stmt = it.prepareStatement("""
                SELECT
                    r.id as ride_id,
                    r.name as ride_name,
                    r.altname as ride_altname,
                    p.uuid as player_uuid,
                    p.username as player_username,
                    p.hidden as player_hidden,
                    t.total as total
                FROM ridecount_total t
                JOIN rides r ON t.ride_id = r.id
                JOIN players p ON t.player_uuid = p.uuid
                WHERE t.player_uuid = ? AND t.ride_id = ?
                LIMIT 1
            """.trimIndent())
            stmt.setString(1, uuid)
            stmt.setString(2, rideId)
            val resultSet = stmt.executeQuery()
            if (!resultSet.next()) {
                return@withContext null
            }
            val ride = mapRide(resultSet) ?: return@withContext null
            val player = mapPlayer(resultSet) ?: return@withContext null
            val total = resultSet.getInt("total")
            record = RidecountTotalRecord(
                player, ride, total
            )
        }
        return@withContext record
    }

    suspend fun getTopTotalRidecounter(rideId: String, limit: Int): RidecountTopRecord? = withContext(Ridecounters.asyncDispatcher) {
        val ride: RideRecord
        val data = mutableListOf<RidecountTotalRecord>()
        connection().use {
            val rideStmt = it.prepareStatement("SELECT id as ride_id, name as ride_name, altname as ride_altname FROM rides WHERE id = ?")
            rideStmt.setString(1, rideId)
            val rideResult = rideStmt.executeQuery()
            if (!rideResult.next()) {
                return@withContext null
            }
            ride = mapRide(rideResult) ?: return@withContext null
            val stmt = it.prepareStatement(
                """
                SELECT
                    p.uuid as player_uuid,
                    p.username as player_username,
                    p.hidden as player_hidden,
                    t.total as total
                FROM ridecount_total t
                JOIN players p ON p.uuid = t.player_uuid
                WHERE t.ride_id = ? AND p.hidden = false
                ORDER BY t.total DESC
                LIMIT ?
                """.trimMargin()
            )
            stmt.setString(1, rideId)
            stmt.setInt(2, limit)
            val resultSet = stmt.executeQuery()
            while (resultSet.next()) {
                val player = mapPlayer(resultSet) ?: continue
                data.add(
                    RidecountTotalRecord(
                        player,
                        ride,
                        resultSet.getInt("total")
                    )
                )
            }
        }
        return@withContext RidecountTopRecord(ride, data.toSet())
    }

    suspend fun savePlayer(player: Player) = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            val saveStmt = when (connector.type) {
                DBType.MARIADB -> "INSERT INTO players (uuid, username) VALUES (?, ?) ON DUPLICATE KEY UPDATE username = ?"
                DBType.SQLITE -> "INSERT INTO players (uuid, username) VALUES (?, ?) ON CONFLICT DO UPDATE SET username = ?"
            }
            val stmt = it.prepareStatement(saveStmt)
            stmt.setString(1, player.uniqueId.toString())
            stmt.setString(2, player.name)
            stmt.setString(3, player.name)
            stmt.executeUpdate()
        }
    }

    suspend fun getRides(idMatching: String): List<RideRecord> = withContext(Ridecounters.asyncDispatcher) {
        val list = mutableListOf<RideRecord>()
        connection().use {
            val stmt = it.prepareStatement("""SELECT id as ride_id, name as ride_name, altname as ride_altname FROM rides WHERE id LIKE ? LIMIT 20""")
            stmt.setString(1, "%$idMatching%")
            val results = stmt.executeQuery()
            while (results.next()) {
                list.add(
                    mapRide(results) ?: continue
                )
            }
        }
        return@withContext list
    }

    suspend fun getRides(limit: Int, offset: Int): List<RideRecord> = withContext(Ridecounters.asyncDispatcher) {
        val list = mutableListOf<RideRecord>()
        connection().use {
            val stmt = it.prepareStatement("SELECT id as ride_id, name as ride_name, altname as ride_altname FROM rides LIMIT ? OFFSET ?")
            stmt.setInt(1, limit)
            stmt.setInt(2, offset)
            val results = stmt.executeQuery()
            while (results.next()) {
                list.add(
                    mapRide(results) ?: continue
                )
            }
        }
        return@withContext list.toList()
    }

    suspend fun getRide(rideId : String) : RideRecord? = withContext(Ridecounters.asyncDispatcher) {
        val ride: RideRecord?
        connection().use {
            val stmt = it.prepareStatement("SELECT id as ride_id, name as ride_name, altname as ride_altname FROM rides WHERE id = ?")
            stmt.setString(1, rideId)
            val resultSet = stmt.executeQuery()
            ride = if (resultSet.next()) { mapRide(resultSet) } else null
        }
        return@withContext ride
    }

    suspend fun changeName(rideId: String, name: String) = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            val stmt = it.prepareStatement("UPDATE rides SET name = ? WHERE id = ?")
            stmt.setString(1, name)
            stmt.setString(2, rideId)
            return@withContext stmt.executeUpdate() > 0
        }
    }

    suspend fun changeAltName(rideId: String, name: String) = withContext(Ridecounters.asyncDispatcher) {
        connection().use {
            val stmt = it.prepareStatement("UPDATE rides SET altname = ? WHERE id = ?")
            stmt.setString(1, name)
            stmt.setString(2, rideId)
            return@withContext stmt.executeUpdate() > 0
        }
    }

    private fun mapPlayer(resultSet: ResultSet): PlayerRecord? {
        return try {
            PlayerRecord(
                UUID.fromString(resultSet.getString("player_uuid")),
                resultSet.getString("player_username"),
                resultSet.getBoolean("player_hidden")
            )
        } catch (_: Exception) { null }
    }

    private fun mapRide(resultSet: ResultSet): RideRecord? {
        return try {
            RideRecord(
                resultSet.getString("ride_id"),
                resultSet.getString("ride_name"),
                resultSet.runCatching { getString("ride_altname") }.getOrNull()
            )
        } catch (_: Exception) { null }
    }
}