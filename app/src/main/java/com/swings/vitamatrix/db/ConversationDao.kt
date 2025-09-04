package com.swings.vitamatrix.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swings.vitamatrix.models.Conversation

@Dao
interface ConversationDao {
    @Insert
    fun insertConversation(conversation: Conversation): Long

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): List<Conversation>
}
