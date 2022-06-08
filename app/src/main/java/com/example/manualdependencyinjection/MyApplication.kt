package com.example.manualdependencyinjection

import android.app.Activity
import android.app.Application

class MyApplication : Application() {

    // AppContainer 는 앱 전반에서 사용될 것이라 여기에 선언 함
    val appContainer = AppContainer()

    var activity: Activity? = null
}