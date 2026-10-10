package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoReplyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AutoReplyLogEntity): Long

    @Query("SELECT * FROM auto_reply_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AutoReplyLogEntity>>

    @Query("SELECT * FROM auto_reply_logs WHERE action = :action ORDER BY timestamp DESC")
    fun getLogsByAction(action: String): Flow<List<AutoReplyLogEntity>>

    @Query("SELECT COUNT(*) FROM auto_reply_logs")
    fun getTotalLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM auto_reply_logs WHERE action = 'REPLY'")
    fun getReplyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM auto_reply_logs WHERE action = 'HANDOFF'")
    fun getHandoffCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM auto_reply_logs WHERE action = 'NO_REPLY'")
    fun getNoReplyCount(): Flow<Int>

    @Query("DELETE FROM auto_reply_logs")
    suspend fun clearAll()

    @Query("DELETE FROM auto_reply_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
