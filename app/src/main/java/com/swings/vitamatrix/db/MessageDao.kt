package com.swings.vitamatrix.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.swings.vitamatrix.models.Conversation
import com.swings.vitamatrix.models.Message

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message)

//    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
//    suspend fun getMessagesByConversationId(conversationId: Int): List<Message>

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT 1")
    fun getLastConversation(): LiveData<Conversation>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversationIdLive(conversationId: Int): LiveData<List<Message>>
}
