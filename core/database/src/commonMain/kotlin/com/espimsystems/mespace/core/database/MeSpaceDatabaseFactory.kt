package com.espimsystems.mespace.core.database

class MeSpaceDatabaseFactory(
    private val driverFactory: DatabaseDriverFactory,
) {

    fun createDatabase(): MeSpaceDatabase {
        return MeSpaceDatabase(
            driver = driverFactory.createDriver(),
        )
    }
}
