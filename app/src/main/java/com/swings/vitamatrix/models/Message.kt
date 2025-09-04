package com.swings.vitamatrix.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: Int,
    val role: String,
    val content: String,
    val timestamp: Long
) {
    @Ignore
    constructor(
        conversationId: Int,
        role: String,
        content: String
    ) : this(
        conversationId = conversationId,
        role = role,
        content = content,
        timestamp = System.currentTimeMillis()
    )
}