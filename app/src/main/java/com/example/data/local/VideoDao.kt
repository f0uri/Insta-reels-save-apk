package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM downloaded_videos ORDER BY downloadTimestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM downloaded_videos ORDER BY downloadTimestamp DESC LIMIT 5")
    fun getRecentVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM downloaded_videos WHERE platform = :platform ORDER BY downloadTimestamp DESC")
    fun getVideosByPlatform(platform: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Query("DELETE FROM downloaded_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM downloaded_videos")
    suspend fun clearAll()
}
