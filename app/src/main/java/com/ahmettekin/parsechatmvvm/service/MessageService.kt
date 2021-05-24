package com.ahmettekin.parsechatmvvm.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.database.MessagesDatabase
import com.ahmettekin.parsechatmvvm.isApplicationPaused
import com.ahmettekin.parsechatmvvm.model.Message
import com.ahmettekin.parsechatmvvm.view.activity.MainActivity
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class MessageService : Service(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onDestroy() {}

    override fun onCreate() {
        super.onCreate()
        getMessages()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun getMessages() {
        val query: ParseQuery<ParseObject> = ParseQuery.getQuery("Message")
        query.orderByDescending("createdAt")
        query.findInBackground { messages, e ->
            if (e == null) {
                saveMessagesInSQLite(messages)
                handleMessage()
            } else {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleMessage() {
        val parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
        val parseQuery: ParseQuery<ParseObject> = ParseQuery.getQuery("Message")
        parseQuery.orderByDescending("createdAt")
        val subscriptionHandling: SubscriptionHandling<ParseObject> = parseLiveQueryClient.subscribe(parseQuery)
        subscriptionHandling.handleEvents { query, _, _ ->
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                query?.findInBackground { messages, e ->
                    if (e == null) {
                        saveMessagesInSQLite(messages)
                        if (!isMyMessage(messages.first().getString("userId"))) {
                            createNotification(messages.first().getString("userId") ?: "",messages.first().getString("body") ?: "")
                        }
                    } else {
                        Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun sendNotification(message: String?) {
        message?.let {
            val mMessage = if (!isApplicationPaused || isApplicationPaused == null ) {
                ""
            } else {
                message
            }
            val builder: NotificationCompat.Builder
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "kanalId"
                val channelName = "kanalAd"
                val channelDescription = "kanalTanıtım"
                val channelImportance = NotificationManager.IMPORTANCE_HIGH
                var notificationChannel: NotificationChannel? =
                    notificationManager.getNotificationChannel(channelId)
                if (notificationChannel == null) {
                    notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
                    notificationChannel.description = channelDescription
                    notificationManager.createNotificationChannel(notificationChannel)
                }
                builder = NotificationCompat.Builder(this, channelId)
                builder.setContentTitle("New Message")
                    .setContentText(mMessage)
                    .setSmallIcon(R.drawable.ic_baseline_message_24)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
            } else {
                builder = NotificationCompat.Builder(this)
                builder.setContentTitle("New Message")
                    .setContentText(mMessage)
                    .setSmallIcon(R.drawable.ic_baseline_message_24)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .priority = Notification.PRIORITY_HIGH
            }
            notificationManager.notify(1, builder.build())
        }
    }

    private fun isMyMessage(messageUserId: String?): Boolean {
        return ParseUser.getCurrentUser().objectId == messageUserId
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun saveMessagesInSQLite(list: MutableList<ParseObject>) {
        launch {
            val dao = MessagesDatabase(getApplication()).messagesDao()
            runBlocking {
                dao.deleteAllMessages()
            }
            val messages = arrayListOf<Message>()
            for (tempMessage in list) {
                val message = Message(
                    tempMessage.objectId,
                    tempMessage.getString("userId"),
                    tempMessage.getString("body")
                )
                messages.add(message)
            }
            messages.reverse()
            dao.insertAll(*messages.toTypedArray())
        }
    }

    private fun createNotification(title:String, body:String){
        var message = ""
        val data = JSONObject()
        try {
            if (isApplicationPaused) message = body
            data.put("alert", message)
            data.put("title", title)
        } catch (e: JSONException) {
            throw IllegalArgumentException("unexpected parsing error", e)
        }
        val push = ParsePush()
        val pushQuery = ParseInstallation.getQuery()
        pushQuery.whereEqualTo("user",ParseUser.getCurrentUser())
        push.setQuery(pushQuery)
        push.setMessage(message)
        push.sendInBackground()
    }

}

