package com.swings.vitamatrix.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swings.vitamatrix.models.Conversation
import com.swings.vitamatrix.models.Message
import com.swings.vitamatrix.models.QAData

@Database(entities = [Conversation::class, Message::class, QAData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun qaDao(): QADataDao
}
