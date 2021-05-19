package com.ahmettekin.parsechatmvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

internal var isApplicationPaused = true

infix fun <T> Boolean.then(param: T): T? = if (this) param else null