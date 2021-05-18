package com.ahmettekin.parsechatmvvm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.databinding.RowChatroomsLayoutBinding
import com.ahmettekin.parsechatmvvm.model.Room
import java.lang.reflect.Array

class RoomsAdapter(private val roomList: ArrayList<Room>, private val mListener : RoomClickListener): RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>(){
    private lateinit var binding: RowChatroomsLayoutBinding

    class RoomViewHolder(var mBinding: RowChatroomsLayoutBinding) : RecyclerView.ViewHolder(mBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.row_chatrooms_layout,parent,false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.mBinding.room = roomList[position]
        holder.mBinding.listener = mListener
    }

    override fun getItemCount()= roomList.size

    fun updateRoomList(newRoomList: List<Room>) {
        roomList.clear()
        roomList.addAll(newRoomList)
        notifyDataSetChanged()
    }

}