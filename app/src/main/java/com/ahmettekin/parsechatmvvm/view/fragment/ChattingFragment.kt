package com.ahmettekin.parsechatmvvm.view.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.adapter.ChatAdapter
import com.ahmettekin.parsechatmvvm.databinding.FragmentChattingBinding
import com.ahmettekin.parsechatmvvm.model.Message
import com.ahmettekin.parsechatmvvm.viewmodel.ChattingViewModel
import com.parse.ParseUser

class ChattingFragment : Fragment() {
    private lateinit var viewModel: ChattingViewModel
    private lateinit var dataBinding: FragmentChattingBinding
    private lateinit var mAdapter: ChatAdapter
    private lateinit var mMessages: ArrayList<Message>
    private lateinit var mView: View
    private var currentRoomObjectId = ""
    private var currentRoomUserIdList = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_chatting,container,false)
        setHasOptionsMenu(true)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        viewModel = ViewModelProviders.of(this).get(ChattingViewModel::class.java)
        val userId = ParseUser.getCurrentUser().objectId
        mMessages = ArrayList()
        mAdapter = ChatAdapter(activity, userId,mMessages)
        arguments?.let{
            currentRoomObjectId= ChattingFragmentArgs.fromBundle(it).currentRoomObjectId
            currentRoomUserIdList= ChattingFragmentArgs.fromBundle(it).currentRoomUserIdList
        }
        dataBinding.rvChat.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }
        dataBinding.fragment = this
        observeLiveData()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_log_out -> {
                ParseUser.logOut()
                val action = ChattingFragmentDirections.actionChattingFragmentToLoginFragment()
                Navigation.findNavController(mView).navigate(action)
                true
            }
            R.id.menu_join_room -> {
                viewModel.joinRoom(currentRoomObjectId)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeLiveData() {
        viewModel.messageList.observe(viewLifecycleOwner,{ messages ->
            messages?.let{
                mAdapter.updateMessages(messages)
                dataBinding.rvChat.scrollToPosition(messages.size-1)
            }
        })

        viewModel.isJoined.observe(viewLifecycleOwner,{ isJoined->
            isJoined?.let {
                if (isJoined){
                    dataBinding.etMessage.visibility = View.VISIBLE
                    dataBinding.btnSend.visibility = View.VISIBLE
                }else{
                    dataBinding.btnSend.visibility = View.GONE
                    dataBinding.etMessage.visibility = View.GONE
                }
            }
        })

        viewModel.isConnected.observe(viewLifecycleOwner,{ isConnected->
            isConnected?.let {
                if(it){
                    viewModel.setupMessagesFromB4a(currentRoomObjectId)
                }else{
                    viewModel.getMessagesFromSQLite(currentRoomObjectId)
                }
            }
        })
    }

    fun sendMessage(){
        viewModel.sendMessage(dataBinding.etMessage.text.toString(), currentRoomObjectId)
    }

    override fun onResume() {
        super.onResume()
        viewModel.joinControl(currentRoomObjectId)
        viewModel.connectionControl(viewLifecycleOwner)
        viewModel.setupMessagesFromB4a(currentRoomObjectId)
        viewModel.changeRoomLive(currentRoomObjectId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.unsubscribe()
    }
    
}