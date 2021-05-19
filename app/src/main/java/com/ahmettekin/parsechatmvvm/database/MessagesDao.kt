package com.ahmettekin.parsechatmvvm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.ahmettekin.parsechatmvvm.model.Message

@Dao
interface MessagesDao {

    @Insert
    suspend fun insertAll(vararg messages: Message): List<Long>

    @Query("SELECT * FROM message")
    suspend fun getAllMessages(): List<Message>

    @Query("DELETE FROM message")
    suspend fun deleteAllMessages()
}