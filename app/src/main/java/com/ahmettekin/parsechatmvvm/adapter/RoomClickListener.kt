package com.ahmettekin.parsechatmvvm.adapter

import android.view.View
import com.ahmettekin.parsechatmvvm.generated.callback.OnClickListener

interface RoomClickListener{
    fun onRoomClicked(currentRoomObjectId: String, currentRoomUserIdList: List<String>)
}