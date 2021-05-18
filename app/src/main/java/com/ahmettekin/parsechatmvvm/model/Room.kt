package com.ahmettekin.parsechatmvvm.model

data class Room( val objectId: String?,
                 val name: String?,
                 val adminUserId: String?,
                 var userIdList: ArrayList<String>?,
                 var messageIdList: ArrayList<String>?)
