package com.bagashyt.myintermediate.ui.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bagashyt.myintermediate.ui.auth.AuthActivity
import com.bagashyt.myintermediate.ui.main.MainActivity
import com.bagashyt.myintermediate.ui.main.MainActivity.Companion.EXTRA_TOKEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        determineUserDirection()
    }

    private fun determineUserDirection() {
        lifecycleScope.launchWhenCreated {
            launch {
                viewModel.getAuthToken().collect { token ->
                    if (token.isNullOrEmpty()) {
                        Intent(this@SplashActivity, AuthActivity::class.java).also { intent ->
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Intent(this@SplashActivity, MainActivity::class.java).also { intent ->
                            intent.putExtra(EXTRA_TOKEN, token)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}