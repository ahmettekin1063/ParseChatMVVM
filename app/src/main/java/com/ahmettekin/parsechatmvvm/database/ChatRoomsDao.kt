package com.ahmettekin.parsechatmvvm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ahmettekin.parsechatmvvm.model.ChatRoom

@Dao
interface ChatRoomsDao {

    @Insert(/*onConflict = OnConflictStrategy.ABORT*/)
    suspend fun insertAll(vararg chatrooms: ChatRoom): List<Long>

    @Query("SELECT * FROM chatroom")
    suspend fun getAllChatRooms(): List<ChatRoom>

    @Query("SELECT * FROM chatroom WHERE objectId = :roomId")
    suspend fun getChatroom(roomId: String): ChatRoom

    @Query("DELETE FROM chatroom")
    suspend fun deleteAllChatRooms()
}