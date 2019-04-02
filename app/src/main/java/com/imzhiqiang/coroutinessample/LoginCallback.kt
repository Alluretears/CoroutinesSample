package com.imzhiqiang.coroutinessample

interface LoginCallback {
    fun onLoginSuccess(token: String)
    fun onLoginFailure()
}