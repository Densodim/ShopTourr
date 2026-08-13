package com.example.shoptourr.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.shoptourr.data.settings.SecureKeyValueStore
import com.example.shoptourr.db.VoyageDatabase
import java.io.File
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

actual class DatabaseDriverFactory(
    private val context: Context,
    private val secureStore: SecureKeyValueStore,
) {
    actual fun createDriver(): SqlDriver {
        System.loadLibrary("sqlcipher")
        val passphrase = DatabaseEncryptionKey.getOrCreate(secureStore)
        val encrypted = context.getDatabasePath(EncryptedDatabasePolicy.ENCRYPTED_FILENAME)
        val plaintext = context.getDatabasePath(EncryptedDatabasePolicy.PLAINTEXT_FILENAME)
        when (EncryptedDatabasePolicy.openPlan(encrypted.exists(), plaintext.exists())) {
            EncryptedDatabaseOpenPlan.MIGRATE_PLAINTEXT -> migratePlaintext(plaintext, encrypted, passphrase)
            EncryptedDatabaseOpenPlan.OPEN_ENCRYPTED,
            EncryptedDatabaseOpenPlan.CREATE_ENCRYPTED,
            -> Unit
        }
        return encryptedDriver(passphrase)
    }

    private fun encryptedDriver(passphrase: String): SqlDriver =
        AndroidSqliteDriver(
            schema = VoyageDatabase.Schema,
            context = context,
            name = EncryptedDatabasePolicy.ENCRYPTED_FILENAME,
            factory = SupportOpenHelperFactory(passphrase.toByteArray(Charsets.UTF_8)),
        )

    private fun migratePlaintext(plaintext: File, encrypted: File, passphrase: String) {
        deleteSqliteFiles(encrypted)
        val db = SQLiteDatabase.openOrCreateDatabase(plaintext, null)
        try {
            db.rawExecSQL(
                EncryptedDatabasePolicy.attachEncryptedSql(encrypted.absolutePath, passphrase),
            )
            db.rawExecSQL(EncryptedDatabasePolicy.SQLCIPHER_EXPORT)
            db.rawExecSQL(EncryptedDatabasePolicy.DETACH_ENCRYPTED)
        } catch (_: Throwable) {
            deleteSqliteFiles(encrypted)
            deleteSqliteFiles(plaintext)
            return
        } finally {
            db.close()
        }
        if (encrypted.exists()) {
            deleteSqliteFiles(plaintext)
        }
    }

    private fun deleteSqliteFiles(file: File) {
        EncryptedDatabasePolicy.sqliteSidecarSuffixes.forEach { suffix ->
            File(file.path + suffix).delete()
        }
    }
}
