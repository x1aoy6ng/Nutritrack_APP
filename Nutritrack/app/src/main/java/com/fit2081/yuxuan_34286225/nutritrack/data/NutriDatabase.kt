package com.fit2081.yuxuan_34286225.nutritrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fit2081.yuxuan_34286225.nutritrack.data.chathistory.ChatHistory
import com.fit2081.yuxuan_34286225.nutritrack.data.chathistory.ChatHistoryDao
import com.fit2081.yuxuan_34286225.nutritrack.data.foodintake.FoodIntake
import com.fit2081.yuxuan_34286225.nutritrack.data.foodintake.FoodIntakeDao
import com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips.NutriCoachTips
import com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips.NutriCoachTipsDao
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.Patient
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientDao

@Database(entities = [Patient::class, FoodIntake::class, NutriCoachTips::class, ChatHistory::class], version = 1, exportSchema = false)
/**
 * Abstract class representing NutriTrack Database
 * Extends RoomDatabase and provide access to DAO interfaces for the entities
 */
abstract class NutriDatabase: RoomDatabase(){
    /**
     * Provides access to the PatientDao interface for performing database operation on Patient entities
     * @return PatientDao instances
     */
    abstract fun patientDao(): PatientDao

    /**
     * Provides access to the FoodIntakeDao interface for performing database operation on FoodIntake entities
     * @return FoodIntakeDao instances
     */
    abstract fun foodIntakeDao(): FoodIntakeDao

    /**
     * Provides access to the NutriCoachTips interface for performing database operation on FoodIntake entities
     * @return NutriCoachTips instances
     */
    abstract fun nutriCoachTipsDao(): NutriCoachTipsDao

    /**
     * Provides access to the ChatHistoryDao interface for performing database operation on FoodIntake entities
     * @return ChatHistoryDao instances
     */
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object{
        /**
         * volatile variable that holds the database instance
         */
        @Volatile
        private var Instance: NutriDatabase? = null

        /**
         * Retrieves the singleton instance of database
         * If instance is null, it creates new database instance
         * @param context The context of the application
         * @return The singleton instance of NutriDatabase
         */
        fun getDatabase(context: Context): NutriDatabase{
            return Instance?: synchronized(this){
                Room.databaseBuilder(context, NutriDatabase::class.java, "nutritrack_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}