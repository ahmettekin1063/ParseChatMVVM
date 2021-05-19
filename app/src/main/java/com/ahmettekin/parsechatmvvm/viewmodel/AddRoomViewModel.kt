package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ahmettekin.parsechatmvvm.service.MessageService
import com.parse.ParseObject
import com.parse.ParseUser
import kotlinx.coroutines.launch

class AddRoomViewModel(application: Application) : BaseViewModel(application) {

    fun createRoomInBackground(roomName: String?) {
        launch {
            roomName?.let {
                val room = ParseObject("Rooms")
                room.put("name", it)
                room.put("messageIdList", "")
                room.put("adminUserId", ParseUser.getCurrentUser().objectId)
                room.put("userIdList", ParseUser.getCurrentUser().objectId)
                room.saveInBackground { error ->
                    if (error != null) {
                        println(error.localizedMessage)
                        Toast.makeText(getApplication(), error.localizedMessage, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(getApplication(), "Room Created!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

}