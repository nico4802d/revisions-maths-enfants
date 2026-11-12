package com.nico.revisionsmaths

import android.app.Application
import android.content.Context
import com.nico.revisionsmaths.data.SettingsStore
import com.nico.revisionsmaths.data.db.AppDatabase
import com.nico.revisionsmaths.voice.AiClient

class RevisionsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.get(context)
    val settings: SettingsStore = SettingsStore(context)
    val aiClient: AiClient = AiClient(settings)
}
