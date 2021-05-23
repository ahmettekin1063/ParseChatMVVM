package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.ahmettekin.parsechatmvvm.database.ChatRoomsDatabase
import com.ahmettekin.parsechatmvvm.database.MessagesDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.ahmettekin.parsechatmvvm.model.Message
import com.ahmettekin.parsechatmvvm.util.NetworkConnectionLiveData
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ChattingViewModel(application: Application) : BaseViewModel(application) {
    val messageList = MutableLiveData<List<Message>>()
    val isJoined = MutableLiveData<Boolean>()
    val isConnected = MutableLiveData<Boolean>()
    private val roomLiveQueryClient: ParseLiveQueryClient= ParseLiveQueryClient.Factory.getClient()
    private val messageQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Message")
    private val roomQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
    private lateinit var roomsSubscriptionHandling: SubscriptionHandling<ParseObject>

    fun connectionControl(viewLifecycleOwner: LifecycleOwner) {
        NetworkConnectionLiveData(getApplication())
            .observe(viewLifecycleOwner,{ isOnline ->
                isConnected.value = isOnline
            })
    }

    fun sendMessage(sentMessage: String, currentRoomObjectId: String) {
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

    private fun createNotification(title:String, body:String){
        val data = JSONObject()
        try {
            data.put("alert", body)
            data.put("title", title)
        } catch (e: JSONException) {
            throw IllegalArgumentException("unexpected parsing error", e)
        }
        val push = ParsePush()
        val pushQuery = ParseInstallation.getQuery()
        pushQuery.whereEqualTo("user",ParseUser.getCurrentUser())
        push.setQuery(pushQuery)
        push.setMessage(body)
        push.sendInBackground {
            it?.let {
                it.localizedMessage?.let {
                    println(it)
                }
            }
        }
    }

    private fun saveMessageIdToCurrentRoom(messageId: String, currentRoomObjectId: String) {
        roomQuery.getInBackground(currentRoomObjectId) { room, e ->
            var tempMessageIds = room.getString("messageIdList")
            tempMessageIds = "$tempMessageIds$messageId"
            room.put("messageIdList", tempMessageIds)
            room.saveInBackground()
        }
    }

    fun setupMessagesFromB4a(currentRoomObjectId: String) {
        messageQuery.orderByDescending("createdAt")
        messageQuery.findInBackground { messages, e ->
            if (e == null) {
                getMessagesInCurrentRoomFromB4a(messages, currentRoomObjectId)
            } else {
                Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMessagesInCurrentRoomFromB4a(messages: MutableList<ParseObject>, currentRoomObjectId: String) {
        roomQuery.getInBackground(currentRoomObjectId) { room, error ->
            val mMessages = arrayListOf<Message>()
                for (tempMessage in messages) {
                    if (room.getString("messageIdList")?.contains(tempMessage.objectId) == true) {
                        val message = Message(tempMessage.objectId, tempMessage.getString("userId"), tempMessage.getString("body"))
                        mMessages.add(message)
                    }
                }
            mMessages.reverse()
            messageList.value = mMessages
        }
    }

    fun unsubscribe(){
        roomLiveQueryClient.unsubscribe(roomQuery, roomsSubscriptionHandling)
    }

    fun joinControl(currentRoomObjectId: String) {
        roomQuery.getInBackground(currentRoomObjectId) { room, e ->
            if (e == null) {
                val userIdList = room.getString("userIdList")
                isJoined.value = userIdList?.contains(ParseUser.getCurrentUser().objectId) != false
            } else {
                Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun joinRoom(currentRoomObjectId: String) {
        roomQuery.getInBackground(currentRoomObjectId) { room, e ->
            if (e == null) {
                var userIdList = room.getString("userIdList")
                userIdList = "$userIdList,${ParseUser.getCurrentUser().objectId}"
                room.put("userIdList", userIdList)
                room.saveInBackground {
                    if (it == null) {
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

    fun getMessagesFromSQLite(currentRoomObjectId: String) {
        launch {
            val messagesDao = MessagesDatabase(getApplication()).messagesDao()
            val messages = messagesDao.getAllMessages()
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

    fun changeRoomLive(currentRoomObjectId: String){
        roomQuery.orderByAscending("createdAt")
        roomsSubscriptionHandling = roomLiveQueryClient.subscribe(roomQuery)
        roomsSubscriptionHandling.handleEvents { query, _, _ ->
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                query?.getInBackground(currentRoomObjectId)
                { currentRoom, e -> getMessagesFromIds(currentRoom) }
            }
        }
    }

    private fun getMessagesFromIds(currentRoom: ParseObject?) {
        messageQuery.orderByDescending("createdAt")
        messageQuery.findInBackground { messages, e ->
            if (e == null) {
                val mMessages = arrayListOf<Message>()
                for (tempMessage in messages) {
                    if (currentRoom?.getString("messageIdList")?.contains(tempMessage.objectId) == true) {
                        val message = Message(tempMessage.objectId, tempMessage.getString("userId"), tempMessage.getString("body"))
                        mMessages.add(message)
                    }
                }
                mMessages.reverse()
                messageList.value = mMessages
                if(!mMessages.isNullOrEmpty() && mMessages.last().userId != ParseUser.getCurrentUser().objectId) {
                    createNotification(mMessages.last().userId ?: "", mMessages.last().body ?: "")
                }
            } else {
                Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}