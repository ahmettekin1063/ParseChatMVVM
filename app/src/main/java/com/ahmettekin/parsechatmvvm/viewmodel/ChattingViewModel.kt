package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.ahmettekin.parsechatmvvm.database.ChatRoomsDatabase
import com.ahmettekin.parsechatmvvm.database.MessagesDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.ahmettekin.parsechatmvvm.model.Message
import com.ahmettekin.parsechatmvvm.util.NetworkConnectionLiveData
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChattingViewModel(application: Application) : BaseViewModel(application) {
    val messageList = MutableLiveData<List<Message>>()
    val isJoined = MutableLiveData<Boolean>()
    val isConnected = MutableLiveData<Boolean>()

    fun connectionControl(viewLifecycleOwner: LifecycleOwner) {
        NetworkConnectionLiveData(getApplication())
            .observe(viewLifecycleOwner,{ isOnline ->
                isConnected.value = isOnline
            })
    }

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
                var tempMessageIds = room.getString("messageIdList")
                tempMessageIds = "$tempMessageIds,$messageId"
                room.put("messageIdList", tempMessageIds)
                room.saveInBackground()
            }
        }
    }

    fun setupMessagesFromB4a(currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Message")
            query.orderByAscending("createdAt")
            query.findInBackground { messages, e ->
                if (e == null) {
                    getMessagesInCurrentRoomFromB4a(messages, currentRoomObjectId)
                } else {
                    Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
            changeChatLive(currentRoomObjectId)
        }
    }

    private fun getMessagesInCurrentRoomFromB4a(messages: MutableList<ParseObject>, currentRoomObjectId: String) {
        launch {
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.getInBackground(currentRoomObjectId) { room, e ->
                val mMessages = arrayListOf<Message>()
                for (tempMessage in messages) {
                    if (room.getString("messageIdList")?.contains(tempMessage.objectId) == true) {
                        val message = Message(tempMessage.objectId,tempMessage.getString("userId"), tempMessage.getString("body"))
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
                            getMessagesInCurrentRoomFromB4a(messages, currentRoomObjectId)
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
                    val userIdList = room.getString("userIdList")
                    isJoined.value = userIdList?.contains(ParseUser.getCurrentUser().objectId) != false
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
                    var userIdList = room.getString("userIdList")
                    userIdList = "$userIdList,${ParseUser.getCurrentUser().objectId}"
                    room.put("userIdList", userIdList)
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

    fun getMessagesFromSQLite(currentRoomObjectId: String) {
        launch {
            val messagesDao = MessagesDatabase(getApplication()).messagesDao()
            val messages = messagesDao.getAllMessages()
            println("get messages from sqlite")
            val roomsDao = ChatRoomsDatabase(getApplication()).chatRoomsDao()
            val room = roomsDao.getChatroom(currentRoomObjectId)
            val mMessages = arrayListOf<Message>()
            for (tempMessage in messages) {
                if (room.messageIdList?.contains(tempMessage.objectId) == true) {
                    mMessages.add(tempMessage)
                }
            }
            messageList.value = mMessages
        }
    }

}