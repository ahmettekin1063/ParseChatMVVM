package com.ahmettekin.parsechatmvvm.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.databinding.FragmentAddRoomBinding
import com.ahmettekin.parsechatmvvm.service.MessageService
import com.ahmettekin.parsechatmvvm.viewmodel.AddRoomViewModel
import com.ahmettekin.parsechatmvvm.viewmodel.LoginViewModel
import com.parse.ParseObject
import com.parse.ParseUser

class AddRoomDialogFragment: DialogFragment(), View.OnClickListener {
    lateinit var mContext: Context
    private lateinit var dataBinding: FragmentAddRoomBinding
    private lateinit var viewModel: AddRoomViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_add_room,container,false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext= view.context
        dataBinding.btnCreateRoom.setOnClickListener(this)
        dataBinding.btnCancel.setOnClickListener(this)
        viewModel = ViewModelProviders.of(this).get(AddRoomViewModel::class.java)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnCreateRoom -> {
                viewModel.createRoomInBackground(dataBinding.etRoomName.text.toString())
            }
        }
        dialog?.dismiss()
    }
}