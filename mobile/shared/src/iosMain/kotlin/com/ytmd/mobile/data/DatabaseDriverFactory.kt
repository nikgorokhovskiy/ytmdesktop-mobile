package com.ytmd.mobile.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ytmd.mobile.data.local.YtmdDatabase

/**
 * iOS SQLDelight driver factory using NativeSqliteDriver.
 */
class DatabaseDriverFactory {

    fun create(): SqlDriver {
        return NativeSqliteDriver(YtmdDatabase.Schema, "ytmd.db")
    }
}
