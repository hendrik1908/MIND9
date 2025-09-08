package com.example.num8rix.database

import kotlin.reflect.KClass

internal fun KClass<AppDatabase>.instantiateImpl(): AppDatabase =
    com.example.num8rix.database.AppDatabase_Impl()
