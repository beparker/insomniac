package com.beparker.insomniac.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PackageDao {
    @Query("SELECT * FROM package ORDER BY label")
    fun getAll(): LiveData<List<Package>>

    @Query("SELECT * FROM package WHERE enabled == 1")
    fun getEnabled(): LiveData<List<Package>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(pkgs: List<Package>)

    @Delete
    fun delete(pkg: Package)

    @Query("DELETE FROM package WHERE name = :name")
    fun delete(name: String)

    @Update
    fun update(pkg: Package)
}