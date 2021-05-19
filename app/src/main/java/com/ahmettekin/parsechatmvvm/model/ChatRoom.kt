package com.ahmettekin.parsechatmvvm.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatRoom(
    @ColumnInfo(name = "objectId")
    val objectId: String,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "adminUserId")
    val adminUserId: String?,

    @ColumnInfo(name = "userIdList")
    var userIdList: String?,
    //var userIdList: ArrayList<String>?,

    @ColumnInfo(name = "messageIdList")
    var messageIdList: String?
    //var messageIdList: ArrayList<String>?

    ){
    @PrimaryKey(autoGenerate = true)
    var uuid=0
}
