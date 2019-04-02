package com.imzhiqiang.coroutinessample

import android.os.SystemClock

class UserServiceImpl : UserService {

    override fun login(email: String, password: String, callback: LoginCallback) {
        if (email == "abc@zech.com" && password == "123456") {
            SystemClock.sleep(3000)
            callback.onLoginSuccess("d6fd4736e859d9f777a2bea36a503b9d")
        } else {
            SystemClock.sleep(1000)
            callback.onLoginFailure()
        }
    }

}