package be.mrtibo.ridecounters.data

import be.mrtibo.ridecounters.Ridecounters.Companion.INSTANCE
import be.mrtibo.ridecounters.utils.Scheduler.async
import be.mrtibo.ridecounters.utils.Scheduler.sync
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

object Database {

    private lateinit var ds: HikariDataSource
    private var isSqlite = false

    init {
        connect()
    }

    fun connectAsync(callback: (Boolean) -> Unit) {
        async {
            callback(connect())
        }
    }

    private fun connect() : Boolean {

        val config = HikariConfig()
        val settings = INSTANCE.config
        if (settings.getString("database.type").equals("sqlite", true)) {
            config.jdbcUrl = "jdbc:sqlite:${INSTANCE.dataFolder}/data.db"
            isSqlite = true
            INSTANCE.logger.info("Using SQLite")
        } else {
            config.jdbcUrl = settings.getString("database.connectionUrl")
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }
        config.connectionTestQuery = "SELECT 1"
        config.connectionTimeout = 5000

        try {
            ds = HikariDataSource(config)
            createTables()

            INSTANCE.logger.info("Datasource initialized")
            return true
        } catch (e: PoolInitializationException) {
            e.printStackTrace()
            return false
        }
    }

    private fun getConnection(): Connection {
        return ds.connection
    }

    private fun createTables() {
        var query1 = """
                    CREATE TABLE IF NOT EXISTS ridecounter_players (
                    id INTEGER AUTO_INCREMENT,
                    uuid CHAR(36) UNIQUE NOT NULL,
                    lastName VARCHAR(24) NOT NULL,
                    PRIMARY KEY (id)
                    );
                    """.trimIndent()
        var query2 = """
                    CREATE TABLE IF NOT EXISTS ridecounter_rides (
                    id INTEGER AUTO_INCREMENT,
                    name TEXT NOT NULL,
                    owningPlayer INT,
                    shortName TEXT,
                    PRIMARY KEY (id),
                    CONSTRAINT fk_ownerPlayer FOREIGN KEY (owningPlayer) REFERENCES ridecounter_players (id) ON UPDATE CASCADE ON DELETE CASCADE
                    );
                    """.trimIndent()
        if (isSqlite) {
            query1 = """
                    CREATE TABLE IF NOT EXISTS ridecounter_players (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid CHAR(36) UNIQUE NOT NULL,
                    lastName VARCHAR(24) NOT NULL
                    );
                    """.trimIndent()
            query2 = """
                    CREATE TABLE IF NOT EXISTS ridecounter_rides (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    owningPlayer INT,
                    shortName TEXT,
                    CONSTRAINT fk_ownerPlayer FOREIGN KEY (owningPlayer) REFERENCES ridecounter_players (id) ON UPDATE CASCADE ON DELETE CASCADE
                    );
                    """.trimIndent()
        }
        val query3 = """
                    CREATE TABLE IF NOT EXISTS ridecounter_data (
                    player INT NOT NULL,
                    ride INT NOT NULL,
                    val INT DEFAULT 0 NOT NULL,
                    CONSTRAINT fk_ride FOREIGN KEY (ride) REFERENCES ridecounter_rides (id) ON UPDATE CASCADE ON DELETE CASCADE,
                    CONSTRAINT fk_player FOREIGN KEY (player) REFERENCES ridecounter_players (id) ON UPDATE CASCADE ON DELETE CASCADE,
                    PRIMARY KEY (player, ride)
                    );
                    """.trimIndent()
        getConnection().use {con ->
            val statement1 = con.prepareStatement(query1)
            val statement2 = con.prepareStatement(query2)
            val statement3 = con.prepareStatement(query3)
            statement1.executeUpdate()
            statement2.executeUpdate()
            statement3.executeUpdate()
        }
        INSTANCE.logger.info("Initiated database tables")
    }

    fun createRideAsync(name: String, ownerUUID: UUID, callback: (String?, String?) -> Unit) {
        async {
            val query = "INSERT INTO ridecounter_rides (name, owningPlayer) VALUES (?, (SELECT id FROM ridecounter_players WHERE uuid = ?))"
            getConnection().use { con ->
                con.autoCommit = false
                val statement = con.prepareStatement(query)
                statement.setString(1, name)
                statement.setString(2, ownerUUID.toString())
                try {
                    statement.executeUpdate()
                    val rs = con.createStatement().executeQuery(if (isSqlite) "SELECT last_insert_rowid()" else "SELECT LAST_INSERT_ID();")
                    rs.next()
                    val id = rs.getInt(1)
                    INSTANCE.logger.info("Added new ride $name to database with ID $id")
                    sync { callback("<green>Added new ride $name <reset><green>to database with ID <yellow>$id", null) }
                } catch (e: SQLException) {
                    con.rollback()
                    INSTANCE.logger.severe(e.message)
                    sync { callback(null, e.message.toString()) }
                }
                con.commit()
            }
        }
    }

    fun deleteRideAsync(rideId: Int, callback: (Int) -> Unit){
        async {
            val query = "DELETE FROM ridecounter_rides WHERE id = ?"
            var result = 0

            getConnection().use { con ->
                val statement = con.prepareStatement(query)
                statement.setInt(1, rideId)
                try {
                    result = statement.executeUpdate()
                    INSTANCE.logger.info("Deleted ride $rideId from database")
                } catch (e: SQLException) {
                    INSTANCE.logger.severe("SQLException while deleting ride from database")
                    INSTANCE.logger.severe(e.message)
                }
            }
            sync { callback(result) }
        }
    }

    fun incrementRideCounter(player: OfflinePlayer, rideId: Int, callback: (Boolean) -> Unit){
        async {
            getConnection().use {con ->
                val statement = when {
                    isSqlite -> con.prepareStatement("""
                        INSERT INTO ridecounter_data (player, ride, val)
                        VALUES ((SELECT id FROM ridecounter_players WHERE uuid = ?), ?, 1)
                        ON CONFLICT(player, ride) DO UPDATE SET val = val + 1
                    """.trimIndent())
                    else -> con.prepareStatement(
                        """
                        INSERT INTO ridecounter_data (player, ride, val)
                        VALUES ((SELECT id FROM ridecounter_players WHERE uuid = ?), ?, 1)
                        ON DUPLICATE KEY UPDATE val = val + 1;
                        """
                    )
                }

                statement.setString(1, player.uniqueId.toString())
                statement.setInt(2, rideId)

                try {
                    statement.executeUpdate()
                    sync { callback(true) }
                } catch (e: SQLException) {
                    sync { callback(false) }
                }
            }
        }

    }

    fun getRideCountAsync(player: Player, rideId: Int, callback: (RideCountEntry?) -> Unit) {
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement("""
                        SELECT players.uuid, players.lastName, rides.name, data.val, rides.shortName
                        FROM ridecounter_data data
                        JOIN ridecounter_players players ON data.player = players.id
                        JOIN ridecounter_rides rides ON data.ride = rides.id
                        WHERE players.uuid = ? AND data.ride = ?;
                        """
                )
                statement.setString(1, player.uniqueId.toString())
                statement.setInt(2, rideId)

                try {
                    val result = statement.executeQuery()
                    if (result.next()) {
                        val ride = Ride(rideId, result.getString(3), result.getString(5))
                        val entry = RideCountEntry(
                            UUID.fromString(result.getString(1)),
                            result.getString(2),
                            ride,
                            result.getInt(4)
                        )
                        sync { callback(entry) }
                    }
                } catch (e: SQLException) {
                    sync { callback(null) }
                }
            }
        }
    }

    fun getTopCountAsync(rideId: Int, limit: Int, callback: (List<RideCountEntry>?) -> Unit) {
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement("""
                SELECT players.uuid, players.lastName, rides.name, data.val, rides.shortName
                FROM ridecounter_data data
                JOIN ridecounter_rides rides ON rides.id = data.ride
                JOIN ridecounter_players players ON players.id = data.player
                WHERE ride = ?
                ORDER BY val DESC
                LIMIT ?
                """)
                statement.setInt(1, rideId)
                statement.setInt(2, limit)

                try {
                    val results = statement.executeQuery()
                    val counts = mutableListOf<RideCountEntry>()
                    while(results.next()) {
                        counts.add(RideCountEntry(UUID.fromString(results.getString(1)), results.getString(2), Ride(rideId, results.getString(3), results.getString(5)), results.getInt(4)))
                    }
                    sync { callback(counts) }
                } catch (e: SQLException) {
                    sync {
                        callback(null)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun getAllRidesAsync(callback: (List<Ride>) -> Unit) {
        async {

            val query = """
                SELECT rides.id, rides.name, players.uuid, players.lastName
                FROM ridecounter_rides rides
                LEFT JOIN ridecounter_players players ON players.id = rides.owningPlayer
            """.trimIndent()
            val list = mutableListOf<Ride>()

            val results : ResultSet
            getConnection().use { con ->
                val statement = con.prepareStatement(query)
                results = statement.executeQuery()
                while(results.next()) {
                    var uuid : UUID? = null
                    try {
                        uuid = UUID.fromString(results.getString(3))
                    } catch (_: Exception) {}
                    var ownerName : String? = "*Server"
                    try {
                        ownerName = results.getString(4)
                    } catch (_: Exception) {}
                    list.add(Ride(
                        id = results.getInt(1),
                        name = results.getString(2),
                        displayName = null,
                        owner = uuid,
                        ownerName = ownerName))
                }
            }
            sync { callback(list) }
        }
    }

    fun rememberPlayer(player: Player) {
        async {
            getConnection().use {con ->
                val query = when {
                    isSqlite -> """
                    INSERT INTO ridecounter_players (uuid, lastName)
                    VALUES (?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET lastName = ?;
                    """.trimIndent()

                    else -> """
                    INSERT INTO ridecounter_players (uuid, lastName)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE lastName = ?;
                    """.trimIndent()
                }
                val statement = con.prepareStatement(query)
                statement.setString(1, player.uniqueId.toString())
                statement.setString(2, player.name)
                statement.setString(3, player.name)
                statement.executeUpdate()
            }
        }
    }

    fun getPlayerRidecounters(player: Player, callback: (List<RideCountEntry>?) -> Unit) {
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement("""
                SELECT rides.id, rides.name, data.val
                FROM ridecounter_data data
                JOIN ridecounter_players players ON players.id = data.player
                JOIN ridecounter_rides rides ON data.ride = rides.id
                WHERE players.uuid = ?
                ORDER BY data.val DESC
            """)

                statement.setString(1, player.uniqueId.toString())
                try {
                    val results = statement.executeQuery()
                    val entries = mutableListOf<RideCountEntry>()
                    while(results.next()) {
                        entries.add(RideCountEntry(player.uniqueId, player.name, Ride(results.getInt(1), results.getString(2), null), results.getInt(3)))
                    }
                    sync {
                        callback(entries)
                    }
                } catch (_: SQLException) {
                    sync {
                        callback(null)
                    }
                }
            }
        }
    }

    fun setName(rideId: Int, newName: String, callback: (Int) -> Unit){
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement("""
                    UPDATE ridecounter_rides
                    SET name = ?
                    WHERE id = ?
                """.trimIndent())
                statement.setString(1, newName)
                statement.setInt(2, rideId)
                try {
                    val result = statement.executeUpdate()
                    sync { callback(result) }
                } catch (e: SQLException) {
                    sync { callback(0)}
                    e.printStackTrace()
                }
            }
        }
    }

    fun setDisplayName(rideId: Int, name: String, callback: (Int) -> Unit) {
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement(
                """
                UPDATE ridecounter_rides
                SET shortName = ?
                WHERE id = ?
                """.trimIndent()
                )
                statement.setString(1, name)
                statement.setInt(2, rideId)
                try {
                    val result = statement.executeUpdate()
                    sync {
                        callback(result)
                    }
                } catch (e: SQLException) {
                    sync {
                        callback(0)
                    }
                    e.printStackTrace()
                }
            }
        }
    }

    fun setRidecount(rideId: Int, player: OfflinePlayer, newValue: Int, callback: (Int) -> Unit) {
        async {
            getConnection().use {con ->
                val statement = con.prepareStatement("""
                    UPDATE ridecounter_data
                    SET val = ?
                    WHERE ride = ? AND player = (SELECT id FROM ridecounter_players WHERE uuid = ?)
                """.trimIndent())
                statement.setInt(1, newValue)
                statement.setInt(2, rideId)
                statement.setString(3, player.uniqueId.toString())
                try {
                    val result = statement.executeUpdate()
                    sync { callback(result) }
                } catch (e: SQLException) {
                    sync { callback(0) }
                    e.printStackTrace()
                }
            }
        }
    }

    fun clearRidedata(rideId: Int, callback: (Int) -> Unit) {
        async {
            getConnection().use {con ->
                val statement = con.prepareStatement("""
                    DELETE FROM ridecounter_data
                    WHERE ride = ?
                """.trimIndent())
                statement.setInt(1, rideId)
                try {
                    val result = statement.executeUpdate()
                    sync { callback(result) }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    sync { callback(0) }
                }
            }
        }
    }

    fun clearRidecount(rideId: Int, player: OfflinePlayer, callback: (Int) -> Unit) {
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement("""
                    DELETE FROM ridecounter_data
                    WHERE ride = ? AND player = (SELECT id FROM ridecounter_players WHERE uuid = ?)
                """.trimIndent())
                statement.apply {
                    setInt(1, rideId)
                    setString(2, player.uniqueId.toString())
                }
                try {
                    val result = statement.executeUpdate()
                    sync { callback(result) }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    sync { callback(0) }
                }
            }
        }
    }

    fun getRide(rideId : Int) : Ride? {
        getConnection().use { con ->
            val statement = con.prepareStatement(
                """
                    SELECT rides.id, rides.name, rides.shortName, players.uuid
                    FROM ridecounter_rides rides
                    LEFT JOIN ridecounter_players players ON players.id = rides.owningPlayer
                    WHERE rides.id = ?
                """.trimIndent()
            )
            statement.setInt(1, rideId)
            try {
                val result = statement.executeQuery()
                if(!result.next()) return null
                var uuid: UUID? = null
                try {
                    uuid = UUID.fromString(result.getString(4))
                } catch (_: Exception) {}
                return Ride(
                    result.getInt(1),
                    result.getString(2),
                    result.getString(3),
                    uuid
                )
            } catch (e: SQLException) {
                return null
            }
        }
    }

    fun getOwnedRides(player: Player, callback: (List<Int>) -> Unit) {
        async {
            getConnection().use { con ->
                val statement = con.prepareStatement("""
                    SELECT rides.id
                    FROM ridecounter_rides rides
                    JOIN ridecounter_players ply ON rides.owningPlayer = ply.id
                    WHERE owningPlayer = (SELECT id FROM ridecounter_players WHERE uuid = ?);
                """.trimIndent())
                statement.setString(1, player.uniqueId.toString())

                try {
                    val result = statement.executeQuery()
                    val list = mutableListOf<Int>()
                    while (result.next()) {
                        list.add(result.getInt(1))
                    }
                    callback(list)
                } catch (_: SQLException) {
                    callback(emptyList())
                }
            }
        }
    }

    private fun getAsyncRide(rideId: Int, callback: (Ride?) -> Unit) {
        async {
            val ride = getRide(rideId) ?: run {
                sync { callback(null) }
                return@async
            }
            sync { callback(ride) }
        }
    }

    fun Player.canAlterRide(rideId: Int, callback: (Ride?, Boolean) -> Unit) {
        getAsyncRide(rideId) {ride ->
            if(ride == null) {
                sync { callback(null, false) }
                return@getAsyncRide
            }
            if(ride.owner == this.uniqueId || this.hasPermission("ridecounters.admin")) callback(ride, true)
            else sync { callback(ride, false) }
        }
    }
}