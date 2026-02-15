package com.ytmd.mobile.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ytmd.mobile.data.local.YtmdDatabase

/**
 * Android SQLDelight driver factory.
 */
class DatabaseDriverFactory(private val context: Context) {

    fun create(): SqlDriver {
        return AndroidSqliteDriver(YtmdDatabase.Schema, context, "ytmd.db")
    }
}
