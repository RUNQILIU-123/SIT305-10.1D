package com.example.llmlearningassistant.data.local

import android.content.Context
import androidx.room.*
import com.example.llmlearningassistant.data.model.HistoryItem
import com.example.llmlearningassistant.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items WHERE username = :username ORDER BY timestamp DESC")
    fun getHistoryForUser(username: String): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE username = :username")
    suspend fun getUserProfileSync(username: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE username = :username")
    fun getUserProfile(username: String): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET currentPlan = :plan WHERE username = :username")
    suspend fun updatePlan(username: String, plan: String)

    @Query("UPDATE user_profiles SET totalQuestions = totalQuestions + :correct + :incorrect, correctAnswers = correctAnswers + :correct, incorrectAnswers = incorrectAnswers + :incorrect WHERE username = :username")
    suspend fun updateStats(username: String, correct: Int, incorrect: Int)
}

@Database(entities = [HistoryItem::class, UserProfileEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learning_assistant_db"
                )
                .fallbackToDestructiveMigration() // Simplified for task development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
