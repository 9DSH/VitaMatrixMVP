package com.swings.vitamatrix.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qa_data")
data class QAData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jsonData: String
)