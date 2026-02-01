package be.mrtibo.ridecounters.data

enum class DBType(val jdbcId: String, val typeName: String) {
    MARIADB("mariadb", "MariaDB"),
    SQLITE("sqlite", "SQLite")
}