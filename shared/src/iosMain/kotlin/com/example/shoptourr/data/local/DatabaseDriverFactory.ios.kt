package com.example.shoptourr.data.local

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.NO_VERSION_CHECK
import com.example.shoptourr.data.settings.SecureKeyValueStore
import com.example.shoptourr.db.VoyageDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual class DatabaseDriverFactory(
    private val secureStore: SecureKeyValueStore,
) {
    actual fun createDriver(): SqlDriver {
        val passphrase = DatabaseEncryptionKey.getOrCreate(secureStore)
        val encryptedPath = databasePath(EncryptedDatabasePolicy.ENCRYPTED_FILENAME)
        val plaintextPath = databasePath(EncryptedDatabasePolicy.PLAINTEXT_FILENAME)
        val fm = NSFileManager.defaultManager
        when (
            EncryptedDatabasePolicy.openPlan(
                encryptedExists = fm.fileExistsAtPath(encryptedPath),
                plaintextExists = fm.fileExistsAtPath(plaintextPath),
            )
        ) {
            EncryptedDatabaseOpenPlan.MIGRATE_PLAINTEXT ->
                migratePlaintext(encryptedPath, passphrase)
            EncryptedDatabaseOpenPlan.OPEN_ENCRYPTED,
            EncryptedDatabaseOpenPlan.CREATE_ENCRYPTED,
            -> Unit
        }
        return encryptedDriver(passphrase)
    }

    private fun encryptedDriver(passphrase: String): SqlDriver =
        NativeSqliteDriver(
            schema = VoyageDatabase.Schema,
            name = EncryptedDatabasePolicy.ENCRYPTED_FILENAME,
            onConfiguration = { config ->
                config.copy(
                    encryptionConfig = DatabaseConfiguration.Encryption(key = passphrase),
                )
            },
        )

    private fun migratePlaintext(encryptedPath: String, passphrase: String) {
        deleteSqliteFiles(encryptedPath)
        val plaintext = NativeSqliteDriver(
            DatabaseConfiguration(
                name = EncryptedDatabasePolicy.PLAINTEXT_FILENAME,
                version = NO_VERSION_CHECK,
                create = {},
                upgrade = { _, _, _ -> },
            ),
        )
        try {
            plaintext.execute(
                identifier = null,
                sql = EncryptedDatabasePolicy.attachEncryptedSql(encryptedPath, passphrase),
                parameters = 0,
            )
            plaintext.executeQuery(
                identifier = null,
                sql = EncryptedDatabasePolicy.SQLCIPHER_EXPORT,
                mapper = { cursor -> QueryResult.Value(cursor.next()) },
                parameters = 0,
            )
            plaintext.execute(
                identifier = null,
                sql = EncryptedDatabasePolicy.DETACH_ENCRYPTED,
                parameters = 0,
            )
        } catch (_: Throwable) {
            deleteSqliteFiles(encryptedPath)
            deleteSqliteFiles(databasePath(EncryptedDatabasePolicy.PLAINTEXT_FILENAME))
            return
        } finally {
            plaintext.close()
        }
        if (NSFileManager.defaultManager.fileExistsAtPath(encryptedPath)) {
            deleteSqliteFiles(databasePath(EncryptedDatabasePolicy.PLAINTEXT_FILENAME))
        }
    }

    private fun databasePath(name: String): String {
        val support = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true,
        ).first() as String
        val dir = "$support/databases"
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(dir)) {
            fm.createDirectoryAtPath(dir, true, null, null)
        }
        return "$dir/$name"
    }

    private fun deleteSqliteFiles(path: String) {
        val fm = NSFileManager.defaultManager
        EncryptedDatabasePolicy.sqliteSidecarSuffixes.forEach { suffix ->
            fm.removeItemAtPath(path + suffix, null)
        }
    }
}
