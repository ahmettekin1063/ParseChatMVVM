package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.room.Room
import com.ahmettekin.parsechatmvvm.database.ChatRoomsDatabase
import com.ahmettekin.parsechatmvvm.model.ChatRoom
import com.ahmettekin.parsechatmvvm.util.NetworkConnectionLiveData
import com.ahmettekin.parsechatmvvm.view.fragment.RoomsFragmentDirections
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.*

class RoomsViewModel(application: Application): BaseViewModel(application) {
    val roomList = MutableLiveData<List<ChatRoom>>()
    val isConnected = MutableLiveData<Boolean>()

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
            val dao = ChatRoomsDatabase(getApplication()).chatRoomsDao()
            val deleted = async {
                dao.deleteAllChatRooms()
            }
            deleted.await()
            dao.insertAll(*list.toTypedArray())
        }

    }

    fun getRoomsFromBack4App(){
        launch {
            val mRoomList = ArrayList<ChatRoom>()
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.orderByAscending("createdAt")
            query.findInBackground { rooms, e ->
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
            changeRoomLive()
        }
    }

    private fun changeRoomLive(){
        launch {
            val parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
            val parseQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            parseQuery.orderByAscending("createdAt")
            val subscriptionHandling: SubscriptionHandling<ParseObject> = parseLiveQueryClient.subscribe(parseQuery)
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