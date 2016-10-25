/**
 * Copyright Google Inc. All Rights Reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package calin.proximity.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import calin.proximity.R
import calin.proximity.R.string.default_web_client_id
import calin.proximity.abstractions.ProximityAuthRepository
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.android.gms.common.api.GoogleApiClient
import com.jakewharton.rxbinding.view.clicks
import kotlinx.android.synthetic.main.activity_sign_in.*
import rx_activity_result.RxActivityResult

class SignInActivity : AppCompatActivity() {
    companion object {
        val TAG = SignInActivity::class.java.canonicalName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //TODO: move in application or make my own observable
        RxActivityResult.register(application)

        val googleApi = googleSignInApi()
        signInWithGoogleButton.clicks()
                .take(1)
                .flatMap { RxActivityResult.on(this).startIntent(Auth.GoogleSignInApi.getSignInIntent(googleApi)) }
                .toSingle()
                .map {
                    var result = Auth.GoogleSignInApi.getSignInResultFromIntent(it.data())
                    when (result.isSuccess || result.signInAccount != null) {
                        true -> result.signInAccount!!
                        else -> throw Exception("Sign in status: ${result.status}")
                    }
                }
                .flatMap { ProximityAuthRepository.signInWithGoogle(it) }
                .subscribe({ user ->
                    Toast.makeText(this, "Welcome ${user.details.displayName}", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, ProximityActivity::class.java))
                }, { error ->
                    Log.e(TAG, "Authentication error: ${error.message}", error)
                })

    }

    private fun googleSignInApi() =
            GoogleApiClient
                    .Builder(this)
                    .enableAutoManage(this, {
                        Log.e(TAG, "Connection error while trying to build GOOGLE_SIGN_IN_API: ${it.errorMessage}")
                    }).addApi(GOOGLE_SIGN_IN_API,
                    GoogleSignInOptions
                            .Builder(DEFAULT_SIGN_IN)
                            .requestIdToken(getString(default_web_client_id))
                            .requestEmail()
                            .build()
            ).build()
}

