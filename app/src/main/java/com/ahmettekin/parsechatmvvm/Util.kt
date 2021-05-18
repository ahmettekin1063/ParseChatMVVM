package com.ahmettekin.parsechatmvvm

internal var isApplicationPaused = true

infix fun <T> Boolean.then(param: T): T? = if (this) param else null