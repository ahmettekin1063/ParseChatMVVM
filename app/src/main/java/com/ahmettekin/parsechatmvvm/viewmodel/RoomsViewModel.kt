package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.ahmettekin.parsechatmvvm.database.ChatRoomsDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.ahmettekin.parsechatmvvm.then
import com.ahmettekin.parsechatmvvm.util.NetworkConnectionLiveData
import com.ahmettekin.parsechatmvvm.view.fragment.RoomsFragmentDirections
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.*

class RoomsViewModel(application: Application): BaseViewModel(application) {
    val roomList = MutableLiveData<List<ChatRoom>>()
    val isConnected = MutableLiveData<Boolean>()
    private val roomQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
    private val roomLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
    private lateinit var subscriptionHandling: SubscriptionHandling<ParseObject>
    private val dao = ChatRoomsDatabase(getApplication()).chatRoomsDao()

    fun connectionControl(viewLifecycleOwner: LifecycleOwner) {
        NetworkConnectionLiveData(getApplication())
            .observe(viewLifecycleOwner,{ isOnline ->
                isConnected.value = isOnline
            })
    }

    fun getRoomsFromSQLite(){
        launch {
            val dao = ChatRoomsDatabase(getApplication()).chatRoomsDao()
            val chatRoomList = dao.getAllChatRooms()
            roomList.value = chatRoomList
        }
    }

    private fun saveRoomsInSQLite(list: List<ChatRoom>){
        launch {
            runBlocking {
                dao.deleteAllChatRooms()
            }
            dao.insertAll(*list.toTypedArray())
        }
    }

    fun getRoomsFromBack4App(){
        launch {
            val mRoomList = ArrayList<ChatRoom>()
            roomQuery.orderByAscending("createdAt")
            roomQuery.findInBackground { rooms, e ->
                if (e == null && rooms != null) {
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
                    roomList.value = mRoomList
                    saveRoomsInSQLite(mRoomList)
                } else {
                    Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun unsubscribe(){
        roomLiveQueryClient.unsubscribe(roomQuery,subscriptionHandling)
    }

    fun changeRoomLive(){
        launch {
            roomQuery.orderByAscending("createdAt")
            subscriptionHandling = roomLiveQueryClient.subscribe(roomQuery)
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
                                roomList.value = mRoomList
                                saveRoomsInSQLite(mRoomList)
                            }
                        } else {
                            Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun logOut(v: View) {
        ParseUser.logOut()
        val action = RoomsFragmentDirections.actionRoomsFragmentToLoginFragment()
        Navigation.findNavController(v).navigate(action)
    }

}