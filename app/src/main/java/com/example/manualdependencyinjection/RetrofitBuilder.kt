package com.example.manualdependencyinjection

class RetrofitBuilder {
    fun baseUrl(s: String): RetrofitBuilder {
        return this
    }

    fun build(): RetrofitBuilder {
        return this
    }

    fun create(clazz: Class<LoginRetrofitService>): LoginRetrofitService {
        return LoginRetrofitService()
    }

}
