package com.ahmettekin.parsechatmvvm.util

import android.app.Application
import com.parse.Parse
import com.parse.ParseInstallation
import com.parse.ParsePush
import com.parse.ParseUser


class StarterApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        Parse.setLogLevel(Parse.LOG_LEVEL_ERROR)
        Parse.initialize(
            Parse.Configuration.Builder(this)
            .applicationId(applicationId)
            .clientKey(clientKey)
            .server(server)
            .build()
        )
        val installation = ParseInstallation.getCurrentInstallation()
        installation.put("GCMSenderId", senderId)
        //installation.put("user", ParseUser.getCurrentUser())
        installation.saveInBackground()
        //ParsePush.subscribeInBackground("Giants")
    }

}