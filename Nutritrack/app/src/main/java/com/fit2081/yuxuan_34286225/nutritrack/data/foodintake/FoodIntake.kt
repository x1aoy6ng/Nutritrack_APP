package com.fit2081.yuxuan_34286225.nutritrack.data.foodintake

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.Patient

@Entity(tableName = "food_intake",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["userId"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["patientId"], unique = true)]
)
data class FoodIntake (
    @PrimaryKey(autoGenerate = true)
    val intakeId: Int = 0,
    val patientId: String,

    val fruit: Boolean,
    val vegetable: Boolean,
    val grain: Boolean,
    val redMeat: Boolean,
    val seafood: Boolean,
    val poultry: Boolean,
    val fish: Boolean,
    val egg: Boolean,
    val nutSeed: Boolean,

    val selectedFoods: String,
    val selectedPersona: String,
    val mealTime: String,
    val sleepTime: String,
    val wakeUpTime: String
)