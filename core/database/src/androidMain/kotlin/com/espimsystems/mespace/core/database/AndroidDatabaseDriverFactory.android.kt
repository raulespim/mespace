package com.espimsystems.mespace.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseDriverFactory(
    private val context: Context,
) : DatabaseDriverFactory {

    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MeSpaceDatabase.Schema,
            context = context,
            name = DATABASE_NAME,
        )
    }

    private companion object {

        const val DATABASE_NAME = "mespace.db"
    }
}
