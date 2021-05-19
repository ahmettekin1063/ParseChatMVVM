package com.ahmettekin.parsechatmvvm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom

@Database(entities = arrayOf(ChatRoom::class), version = 1)
abstract class ChatRoomsDatabase: RoomDatabase() {

    abstract fun chatRoomsDao() : ChatRoomsDao

    companion object {

        @Volatile
        private var instance: ChatRoomsDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, ChatRoomsDatabase::class.java, "rooms"
        ).build()

    }
}