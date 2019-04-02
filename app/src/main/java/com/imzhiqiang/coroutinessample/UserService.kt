package com.imzhiqiang.coroutinessample

interface UserService {
    fun login(email: String, password: String, callback: LoginCallback)
}