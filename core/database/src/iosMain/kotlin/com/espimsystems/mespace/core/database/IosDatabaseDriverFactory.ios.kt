package com.espimsystems.mespace.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDatabaseDriverFactory : DatabaseDriverFactory {

    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = MeSpaceDatabase.Schema,
            name = DATABASE_NAME,
        )
    }

    private companion object {

        const val DATABASE_NAME = "mespace.db"
    }
}
