package com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips

import android.content.Context
import com.fit2081.yuxuan_34286225.nutritrack.data.NutriDatabase
import kotlinx.coroutines.flow.Flow

class NutriCoachTipsRepository(context: Context) {
    // Get the NutriCoachTipsDao instance from database
    private val nutriCoachTipsDao = NutriDatabase.getDatabase(context).nutriCoachTipsDao()

    /**
     * Insert a new [NutriCoachTips] into the database
     */
    suspend fun insertTip(tip: NutriCoachTips){
        return nutriCoachTipsDao.insertTip(tip)
    }

    /**
     * Delete the [NutriCoachTips] from the database
     */
    suspend fun deleteTip(tip: NutriCoachTips){
        return nutriCoachTipsDao.deleteTip(tip)
    }

    /**
     * Get all the nutri coach tips by user Id
     */
    fun getAllTipsById(userId: String): Flow<List<NutriCoachTips>>{
        return nutriCoachTipsDao.getAllTipsById(userId)
    }

}