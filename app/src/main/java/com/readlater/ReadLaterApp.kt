package com.readlater

import android.app.Application
import com.readlater.data.AppContainer
import com.readlater.data.DefaultAppContainer

class ReadLaterApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
