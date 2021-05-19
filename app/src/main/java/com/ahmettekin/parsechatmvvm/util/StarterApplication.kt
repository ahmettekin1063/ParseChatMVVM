package com.ahmettekin.parsechatmvvm.util

import android.app.Application
import android.content.Intent
import com.ahmettekin.parsechatmvvm.service.MessageService
import com.parse.Parse

class StarterApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        Parse.setLogLevel(Parse.LOG_LEVEL_ERROR)
        Parse.initialize(
            Parse.Configuration.Builder(this)
            .applicationId("Qf82oHIQpTVLRcerSbQTiNoqYHwE3jXkvFtSZ9yF")
            .clientKey("h3J6W8FR6p4HsHMIqegOyLorMd0Mo9yiuD0mCrhX")
            .server("https://parsechatmvvm.b4a.io/")
            .build()
        )
    }

}