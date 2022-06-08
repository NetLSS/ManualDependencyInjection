package com.example.manualdependencyinjection

import android.app.Activity
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

/*
단점
- 수많은 보일러플레이트 코드가 필요하다
- 의존성은 순서대로 선언해야 한다
- 재사용시 싱글톤을 사용하게 되는데, 이러면 테스트에서도 같은 객체를 공유해야 한다.
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

// App container 사용
// 전체앱에서 공유되는 객체의 컨테이너
class AppContainer {

    private val retrofit = RetrofitBuilder()
        .baseUrl("https://example.com")
        .build()
        .create(LoginRetrofitService::class.java)

    private val remoteDataSource = UserRemoteDataSource(retrofit)
    private val localDataSource = UserLocalDataSource()

    // 공개용 (이걸 얻기위해 컨테이너를 사용하게됨)
    val userRepository = UserRepository(localDataSource, remoteDataSource)

    val loginViewModelFactory = LoginViewModelFactory(userRepository)

    // 로그인 상황이 아니면 null 이어야 함
    var loginContainer: LoginContainer? = null
}

class LoginActivity2 : Activity() {
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 컨테이너를 application context 에서 가져오고, userRepository 를 가져온다.
        val appContainer = (application as MyApplication).appContainer
        loginViewModel = LoginViewModel(appContainer.userRepository)
    }
}

/*
LoginViewModel 이 여러곳에서 필요한 경우에. 팩토리를 만드는게 의미있을 수 있다.
 */

// 팩토리는 T 타입을 반환하는 생성함수 포함
interface Factory<T>{
    fun create(): T
}

class LoginViewModelFactory(private val userRepository: UserRepository) : Factory<LoginViewModel> {
    override fun create(): LoginViewModel {
        return LoginViewModel(userRepository)
    }
}


class LoginActivity3 : Activity() {
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 컨테이너를 application context 에서 가져오고, userRepository 를 가져온다.
        val appContainer = (application as MyApplication).appContainer
        loginViewModel = appContainer.loginViewModelFactory.create()
    }
}
/*
but, 위 코드의 단점
- AppContainer 를 직접 관ㄹ리해야하고, 모든 종속성에 대한 컨테이너 인스턴스를 직접 만들어야 한다.
- 여전히 많은 보일러플레이트, 인스턴스를 재사용하고 싶은 경우 직접 팩토리나 매개변수를 만들어야 한다.
 */

class LoginContainer(val userRepository: UserRepository) {

    val loginData = LoginUserData() // 로그인 데이터... 샘플

    val loginViewModelFactory = LoginViewModelFactory(userRepository)
}

class LoginUserData {

}


class LoginActivity4 : Activity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var loginUserData: LoginUserData
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as MyApplication).appContainer

        // 로그인 플로우 시작 (loginContainer 를 채워준다)
        appContainer.loginContainer = LoginContainer(appContainer.userRepository)

        loginViewModel = appContainer.loginContainer!!.loginViewModelFactory.create()
        loginUserData = appContainer.loginContainer!!.loginData
    }

    override fun onDestroy() {
        // 로그인 플로우 종료 (컨테이너 제거)
        appContainer.loginContainer = null
        super.onDestroy()
    }
}
