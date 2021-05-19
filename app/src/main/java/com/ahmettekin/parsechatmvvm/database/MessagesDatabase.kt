package com.ahmettekin.parsechatmvvm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.ahmettekin.parsechatmvvm.model.Message

@Database(entities = arrayOf(Message::class), version = 1)
abstract class MessagesDatabase: RoomDatabase() {

    abstract fun messagesDao() : MessagesDao

    companion object {

        @Volatile
        private var instance: MessagesDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, MessagesDatabase::class.java, "messages"
        ).build()

    }
}