package com.beparker.insomniac.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Package(
    @PrimaryKey var name: String,
    var label: String? = null,
    var enabled: Boolean = false
)