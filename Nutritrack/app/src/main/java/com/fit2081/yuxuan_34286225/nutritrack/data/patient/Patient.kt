package com.fit2081.yuxuan_34286225.nutritrack.data.patient
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient (
    @PrimaryKey val userId: String,
    val phoneNum: String,
    // default to empty string
    val userName: String = "",
    val userSex: String,
    val userPassword: String = "",

    // all food scores
    val totalScore: Float,
    val discretionaryScore: Float,
    val vegetableScore: Float,
    val fruitScore: Float,
    val grainsAndCerealScore: Float,
    val wholeGrainScore: Float,
    val meatAndAlternativesScore: Float,
    val diaryScore: Float,
    val sodiumScore: Float,
    val alcoholScore: Float,
    val waterScore: Float,
    val sugarScore: Float,
    val saturatedFatScore: Float,
    val unsaturatedFatScore: Float,

    // get fruit serve size and fruit variation score
    val fruitServeSize: Float,
    val fruitVariation: Float,

    val rememberPassword: Boolean = false
)