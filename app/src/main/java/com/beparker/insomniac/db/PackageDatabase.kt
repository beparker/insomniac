package com.beparker.insomniac.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Package::class], version = 1, exportSchema = true)
abstract class PackageDatabase : RoomDatabase() {
    abstract fun packageDao(): PackageDao

}