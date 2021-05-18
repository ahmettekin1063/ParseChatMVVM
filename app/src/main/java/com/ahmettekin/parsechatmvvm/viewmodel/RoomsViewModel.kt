package com.ahmettekin.parsechatmvvm.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.ahmettekin.parsechatmvvm.model.Room
import com.ahmettekin.parsechatmvvm.view.fragment.LoginFragmentDirections
import com.ahmettekin.parsechatmvvm.view.fragment.RoomsFragmentDirections
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.launch

class RoomsViewModel(application: Application): BaseViewModel(application) {
    val roomList = MutableLiveData<List<Room>>()

    fun getRooms(){
        launch {
            val mRoomList = ArrayList<Room>()
            val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Rooms")
            query.orderByAscending("createdAt")
            query.findInBackground { rooms, e ->
                if (e == null && rooms != null) {
                    for (tempRoom in rooms) {
                        val room = Room(
                            tempRoom.objectId,
                            tempRoom.getString("name"),
                            tempRoom.getString("adminUserId"),
                            tempRoom.getList<String>("userIdList") as ArrayList<String>,
                            tempRoom.getList<String>("messageIdList") as ArrayList<String>
                        )
                        mRoomList.add(room)
                    }
                    roomList.value = mRoomList
                } else {
                    Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
            changeRoomLive()
        }
    }

    fun logOut(v: View) {
        ParseUser.logOut()
        val action = RoomsFragmentDirections.actionRoomsFragmentToLoginFragment()
        Navigation.findNavController(v).navigate(action)
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
                                val mRoomList = ArrayList<Room>()
                                for (tempRoom in it) {
                                    val room = Room(
                                        tempRoom.objectId,
                                        tempRoom.getString("name"),
                                        tempRoom.getString("adminUserId"),
                                        tempRoom.getList<String>("userIdList") as ArrayList<String>,
                                        tempRoom.getList<String>("messageIdList") as ArrayList<String>
                                    )
                                    mRoomList.add(room)
                                }
                                roomList.value = mRoomList
                            }
                        } else {
                            Toast.makeText(getApplication(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

}