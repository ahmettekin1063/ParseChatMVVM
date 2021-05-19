package com.ahmettekin.parsechatmvvm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.model.Message
import com.bumptech.glide.Glide
import java.math.BigInteger
import java.security.MessageDigest

class ChatAdapter(private val mContext: Context, private val mUserId: String, private val mMessages: ArrayList<Message>)
    : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    fun updateMessages(newMessageList: List<Message>) {
        mMessages.clear()
        mMessages.addAll(newMessageList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isMe(position)) {
            MESSAGE_OUTGOING
        } else {
            MESSAGE_INCOMING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            MESSAGE_INCOMING -> {
                val contactView = inflater.inflate(R.layout.message_incoming, parent, false)
                IncomingMessageViewHolder(contactView)
            }
            MESSAGE_OUTGOING -> {
                val contactView = inflater.inflate(R.layout.message_outgoing, parent, false)
                OutgoingMessageViewHolder(contactView)
            }
            else -> {
                throw IllegalArgumentException("Unknown view type")
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = mMessages[position]
        holder.bindMessage(message)
    }

    private fun isMe(position: Int): Boolean {
        val message = mMessages[position]
        return message.userId != null && message.userId == mUserId
    }

    abstract inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindMessage(message: Message)
    }

    inner class IncomingMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val imageOther: ImageView = itemView.findViewById(R.id.ivProfileOther)
        private val body: TextView = itemView.findViewById(R.id.tvBody)
        private val name: TextView = itemView.findViewById(R.id.tvName)

        override fun bindMessage(message: Message) {
            message.userId?.apply {
                Glide.with(mContext)
                    .load(getProfileUrl(this))
                    .circleCrop()
                    .into(imageOther)
                body.text = message.body
                name.text = message.userId
            }
        }

    }

    inner class OutgoingMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val imageMe: ImageView = itemView.findViewById(R.id.ivProfileMe)
        private val body: TextView = itemView.findViewById(R.id.tvBody)

        override fun bindMessage(message: Message) {
            message.userId?.apply {
                Glide.with(mContext)
                    .load(getProfileUrl(this))
                    .circleCrop()
                    .into(imageMe)
                body.text = message.body
            }
        }

    }

    private fun getProfileUrl(userId: String): String {
        var hex = ""
        try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(userId.toByteArray())
            val bigInt = BigInteger(hash)
            hex = bigInt.abs().toString(16)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "https://www.gravatar.com/avatar/$hex?d=identicon"
    }

    companion object {
        private const val MESSAGE_OUTGOING = 123
        private const val MESSAGE_INCOMING = 321
    }
}