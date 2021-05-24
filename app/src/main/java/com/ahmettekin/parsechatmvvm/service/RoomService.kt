package com.ahmettekin.parsechatmvvm.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.ahmettekin.parsechatmvvm.database.ChatRoomsDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RoomService : Service(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {}

    override fun onCreate() {
        super.onCreate()
        getRooms()
    }

    private fun getRooms() {
        val mRoomList = ArrayList<ChatRoom>()
        val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
        query.orderByAscending("createdAt")
        query.findInBackground { rooms, e ->
            if (e == null&& rooms != null) {
                for (tempRoom in rooms) {
                    val room = ChatRoom(
                        tempRoom.objectId,
                        tempRoom.getString("name"),
                        tempRoom.getString("adminUserId"),
                        tempRoom.getString("userIdList"),
                        tempRoom.getString("messageIdList")
                    )
                    mRoomList.add(room)
                }
                observeRooms()
                saveRoomsInSQLite(mRoomList)
            } else {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveRoomsInSQLite(list: ArrayList<ChatRoom>) {
        val dao = ChatRoomsDatabase(getApplication()).chatRoomsDao()
        launch {
            runBlocking {
                dao.deleteAllChatRooms()
            }
            dao.insertAll(*list.toTypedArray())
        }
    }

    private fun observeRooms(){
        val roomQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
        val roomLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
        val subscriptionHandling: SubscriptionHandling<ParseObject> = roomLiveQueryClient.subscribe(roomQuery)
        subscriptionHandling.handleEvents { query, _, _ ->
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                query?.findInBackground { rooms, e ->
                    if (e == null) {
                        rooms?.let {
                            val mRoomList = ArrayList<ChatRoom>()
                            for (tempRoom in it) {
                                val room = ChatRoom(
                                    tempRoom.objectId,
                                    tempRoom.getString("name"),
                                    tempRoom.getString("adminUserId"),
                                    tempRoom.getString("userIdList"),
                                    tempRoom.getString("messageIdList")
                                )
                                mRoomList.add(room)
                            }
                            saveRoomsInSQLite(mRoomList)
                        }
                    } else {
                        Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

}