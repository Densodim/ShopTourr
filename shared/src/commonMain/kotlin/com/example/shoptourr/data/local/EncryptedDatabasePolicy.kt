package com.example.shoptourr.data.local

enum class EncryptedDatabaseOpenPlan {
    OPEN_ENCRYPTED,
    MIGRATE_PLAINTEXT,
    CREATE_ENCRYPTED,
}

object EncryptedDatabasePolicy {
    const val PLAINTEXT_FILENAME = "voyage.db"
    const val ENCRYPTED_FILENAME = "voyage.enc.db"
    const val SQLCIPHER_EXPORT = "SELECT sqlcipher_export('encrypted')"
    const val DETACH_ENCRYPTED = "DETACH DATABASE encrypted"

    val sqliteSidecarSuffixes: List<String> = listOf("", "-wal", "-shm", "-journal")

    fun openPlan(encryptedExists: Boolean, plaintextExists: Boolean): EncryptedDatabaseOpenPlan = when {
        encryptedExists -> EncryptedDatabaseOpenPlan.OPEN_ENCRYPTED
        plaintextExists -> EncryptedDatabaseOpenPlan.MIGRATE_PLAINTEXT
        else -> EncryptedDatabaseOpenPlan.CREATE_ENCRYPTED
    }

    fun attachEncryptedSql(encryptedPath: String, passphrase: String): String {
        val path = escapeSqlLiteral(encryptedPath)
        val key = escapeSqlLiteral(passphrase)
        return "ATTACH DATABASE '$path' AS encrypted KEY '$key'"
    }

    private fun escapeSqlLiteral(value: String): String = value.replace("'", "''")
}
