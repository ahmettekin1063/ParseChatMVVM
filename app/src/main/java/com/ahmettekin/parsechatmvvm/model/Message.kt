package com.ahmettekin.parsechatmvvm.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @ColumnInfo(name = "objectId")
    val objectId: String,
    @ColumnInfo(name = "userId")
    val userId: String?,
    @ColumnInfo(name = "body")
    val body: String?
    ){
    @PrimaryKey(autoGenerate = true)
    var uuid=0
}
