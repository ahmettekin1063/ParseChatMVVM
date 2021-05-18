package com.ahmettekin.parsechatmvvm.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ahmettekin.parsechatmvvm.R
import com.ahmettekin.parsechatmvvm.isApplicationPaused
import com.ahmettekin.parsechatmvvm.service.MessageService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(applicationContext, MessageService::class.java))
    }

    override fun onPause() {
        super.onPause()
        isApplicationPaused = true
    }

    override fun onResume() {
        super.onResume()
        isApplicationPaused = false
    }

    /**
     * eğer kullanıcı odada varsa odaya katıl tıklanılmamalı
     * bildirime tıklanılınca direk mesaja gitmeli
     * bütün mesajlar ve odalar local db ye kaydedilmeli(bu olay internetteki veri isteğinde hata kontrolünde yapılabilir)
     */

}