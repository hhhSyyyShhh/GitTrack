package com.example.gittrack

import android.app.Application
import com.example.gittrack.data.AppContainer

class GitTrackApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
