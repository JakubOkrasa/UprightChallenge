package com.jakubokrasa.uprightchallenge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PostureStat::class], version = 1, exportSchema = false)
abstract class PostureStatDatabase : RoomDatabase() {
    abstract fun postureStatDao(): PostureStatDao

    companion object {
        private var INSTANCE: PostureStatDatabase? = null

        fun getDatabase(context: Context): PostureStatDatabase {
            if (INSTANCE != null) { return INSTANCE!! }
            synchronized(PostureStatDatabase::class) { // todo synchronized is deprecated
                INSTANCE = buildRoomDB(context)
                return INSTANCE!!
            }
        }

        private fun buildRoomDB(context: Context) =
                Room.databaseBuilder(
                        context.applicationContext,
                        PostureStatDatabase::class.java,
                        "uprightchallenge_db"
                ).build()
    }
}