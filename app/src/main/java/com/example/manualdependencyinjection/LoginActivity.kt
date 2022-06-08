package com.example.manualdependencyinjection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/*
- LoginActivity
  - LoginViewModel
    - UserRepository
      - UserLocalDataSource
      - UserRemoteDataSource
        - Retrofit
 */

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // LoginViewModel 의 종속성을 충족시키기 위해 다른 종속성도 연쇄적으로 만족시켜야 한다.
        val retrofit = RetrofitBuilder()
            .baseUrl("https://example.com")
            .build()
            .create(LoginRetrofitService::class.java)

        // UserRepository 의 종속성을 만족 시키자
        val remoteDataSource = UserRemoteDataSource(retrofit)
        val localDataSource = UserLocalDataSource()

        // 이제 UserRepository 를 생성할 수 있다. (LoginViewModel 에 필요한)
        val userRepository = UserRepository(localDataSource, remoteDataSource)

        loginViewModel = LoginViewModel(userRepository)


    }
}

class LoginViewModel(userRepository: UserRepository) {

}

class UserRepository(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource
)

class UserLocalDataSource {

}

class UserRemoteDataSource(
    private val loginRetrofitService: LoginRetrofitService
) {

}

class LoginRetrofitService {

}
