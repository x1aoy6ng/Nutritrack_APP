package com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NutriCoachTipsDao {
    /**
     * Insert a new [NutriCoachTips] into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: NutriCoachTips)

    /**
     * Delete the [NutriCoachTips] from the database
     */
    @Delete
    suspend fun deleteTip(tip: NutriCoachTips)

    /**
     * Get all the nutri coach tips by user Id
     */
    @Query("SELECT * FROM nutriCoachTips WHERE userId = :userId ORDER BY timeStamp DESC")
    fun getAllTipsById(userId: String): Flow<List<NutriCoachTips>>
}