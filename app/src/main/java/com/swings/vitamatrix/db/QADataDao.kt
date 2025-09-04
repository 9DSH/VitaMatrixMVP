package com.swings.vitamatrix.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swings.vitamatrix.models.QAData

@Dao
interface QADataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQAData(data: QAData)

    @Query("SELECT jsonData FROM qa_data WHERE id = 1 LIMIT 1")
    suspend fun getQAData(): String?

    @Query("SELECT jsonData FROM qa_data WHERE id = 2 LIMIT 1")
    suspend fun getHealthJson(): String?

    @Query("SELECT jsonData FROM qa_data WHERE id = 1 LIMIT 1")
    fun getLiveQAData(): LiveData<String?>

    @Query("SELECT jsonData FROM qa_data WHERE id = 2 LIMIT 1")
    fun getLiveHealthJson(): LiveData<String?>

//    @Query("SELECT EXISTS(SELECT 1 FROM qa_data)")
//    suspend fun hasQAData(): Boolean

    @Query("DELETE FROM qa_data WHERE id = 1")
    suspend fun deleteQAData()

    @Query("DELETE FROM qa_data WHERE id = 2")
    suspend fun deleteHealthJson()
}
