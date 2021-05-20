package com.ahmettekin.parsechatmvvm.view.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.isApplicationPaused
import com.ahmettekin.parsechatmvvm.service.MessageService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPause() {
        super.onPause()
        isApplicationPaused = true
    }

    override fun onResume() {
        super.onResume()
        isApplicationPaused = false
    }

}