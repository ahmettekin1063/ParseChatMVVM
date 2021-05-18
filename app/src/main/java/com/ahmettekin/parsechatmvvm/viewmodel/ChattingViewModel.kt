package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ahmettekin.parsechatmvvm.model.Message
import com.ahmettekin.parsechatmvvm.service.MessageService
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.launch

class ChattingViewModel(application: Application) : BaseViewModel(application) {
    val messageList = MutableLiveData<List<Message>>()
    val isJoined = MutableLiveData<Boolean>()

    fun sendMessage(sentMessage: String, currentRoomObjectId: String) {
        launch {
            val message = ParseObject("Message")
            message.put("userId", ParseUser.getCurrentUser().objectId)
            message.put("body", sentMessage)
            message.saveInBackground {
                if (it != null) {
                    Toast.makeText(getApplication(), it.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(getApplication(), "Message Sent!", Toast.LENGTH_LONG).show()
                    saveMessageIdToCurrentRoom(message.objectId, currentRoomObjectId)
                }
            }
        }
    }

    private fun saveMessageIdToCurrentRoom(messageId: String, currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.getInBackground(currentRoomObjectId) { room, e ->
                room.add("messageIdList", messageId)
                room.saveInBackground()
            }
        }
    }

    fun setupMessages(currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Message")
            query.orderByAscending("createdAt")
            query.findInBackground { messages, e ->
                if (e == null) {
                    getMessagesInCurrentRoom(messages, currentRoomObjectId)
                } else {
                    Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
            changeChatLive(currentRoomObjectId)
        }
    }

    private fun getMessagesInCurrentRoom(messages: MutableList<ParseObject>, currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.getInBackground(currentRoomObjectId) { room, e ->
                val mMessages = arrayListOf<Message>()
                for (tempMessage in messages) {
                    if (room.getList<String>("messageIdList")?.contains(tempMessage.objectId) == true) {
                        val message = Message(tempMessage.getString("userId"), tempMessage.getString("body"))
                        mMessages.add(message)
                    }
                }
                messageList.value = mMessages
            }
        }
    }

    private fun changeChatLive(currentRoomObjectId: String) {
        launch {
            val parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
            val parseQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Message")
            parseQuery.orderByAscending("createdAt")
            val subscriptionHandling: SubscriptionHandling<ParseObject> = parseLiveQueryClient.subscribe(parseQuery)
            subscriptionHandling.handleEvents { query, _, _ ->
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    query?.findInBackground { messages, e ->
                        if (e == null) {
                            getMessagesInCurrentRoom(messages, currentRoomObjectId)
                        } else {
                            Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun joinControl(currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.getInBackground(currentRoomObjectId) { room, e ->
                if (e == null) {
                    val userIdList = room.getList<String>("userIdList")
                    userIdList as ArrayList<String>?
                    isJoined.value =
                        userIdList?.contains(ParseUser.getCurrentUser().objectId) != false
                } else {
                    Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun joinRoom(currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.getInBackground(currentRoomObjectId) { room, e ->
                if (e == null) {
                    room.add("userIdList", ParseUser.getCurrentUser().objectId)
                    room.saveInBackground {
                        if (it == null) {
                            println("success")
                            joinControl(currentRoomObjectId)
                        } else {
                            println("error1:" + it.localizedMessage)
                        }
                    }
                } else {
                    println("error2: " + e.localizedMessage)
                }
            }
        }
    }
}