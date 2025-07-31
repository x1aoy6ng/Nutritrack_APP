package com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutriCoachTips")
data class NutriCoachTips (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val tip: String,
    val timeStamp: Long = System.currentTimeMillis()
)