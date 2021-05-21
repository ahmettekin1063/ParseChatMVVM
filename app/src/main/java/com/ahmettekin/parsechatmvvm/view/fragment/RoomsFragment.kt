package com.ahmettekin.parsechatmvvm.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.adapter.RoomClickListener
import com.ahmettekin.parsechatmvvm.adapter.RoomsAdapter
import com.ahmettekin.parsechatmvvm.databinding.FragmentRoomsBinding
import com.ahmettekin.parsechatmvvm.service.MessageService
import com.ahmettekin.parsechatmvvm.viewmodel.RoomsViewModel

class RoomsFragment : Fragment() , RoomClickListener{
    private lateinit var viewModel: RoomsViewModel
    private lateinit var dataBinding: FragmentRoomsBinding
    private lateinit var mView: View
    private val roomsAdapter = RoomsAdapter(arrayListOf(),this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater ,R.layout.fragment_rooms,container,false)
        dataBinding.fragment = this
        setHasOptionsMenu(true)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        viewModel = ViewModelProviders.of(this).get(RoomsViewModel::class.java)
        dataBinding.rvChatRooms.apply {
            adapter = roomsAdapter
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.getRoomsFromBack4App()
        viewModel.connectionControl(viewLifecycleOwner)
        //context?.startService(Intent(context, MessageService::class.java))
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.roomList.observe(viewLifecycleOwner,{ roomList->
            roomList?.let{
                roomsAdapter.updateRoomList(it)
            }
        })
        viewModel.isConnected.observe(viewLifecycleOwner,{ isConnected->
            isConnected?.let {
                if(it){
                    viewModel.getRoomsFromBack4App()
                }else{
                    viewModel.getRoomsFromSQLite()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.rooms_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.rooms_menu_log_out -> {
                viewModel.logOut(mView)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showAddRoomFragment(){
        childFragmentManager.let { AddRoomDialogFragment().show(it,"ADD_ROOM") }
    }

    override fun onRoomClicked(currentRoomObjectId: String, currentRoomUserIdList: String) {
        val action = RoomsFragmentDirections.actionRoomsFragmentToChattingFragment(currentRoomObjectId, currentRoomUserIdList)
        Navigation.findNavController(mView).navigate(action)
    }

}