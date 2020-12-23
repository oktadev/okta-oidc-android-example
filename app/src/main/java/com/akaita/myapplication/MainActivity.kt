package com.akaita.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.okta.oidc.*
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.clients.web.WebAuthClient
import com.okta.oidc.storage.SharedPreferenceStorage
import com.okta.oidc.storage.security.DefaultEncryptionManager
import com.okta.oidc.util.AuthorizationException
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    /**
     * Authorization client using chrome custom tab as a user agent.
     */
    private lateinit var webAuth: WebAuthClient // <1>

    /**
     * The authorized client to interact with Okta's endpoints.
     */
    private lateinit var sessionClient: SessionClient // <2>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupWebAuth()
        setupWebAuthCallback(webAuth)

        signIn.setOnClickListener {
            val payload = AuthenticationPayload.Builder()
                    .build()
            webAuth.signIn(this, payload)
        }
    }

    private fun setupWebAuth() {
        val oidcConfig = OIDCConfig.Builder()
                .clientId("0oa2x1v8cYfnXmR3Z5d6")
                .redirectUri("com.okta.dev-6974382:/callback")
                .endSessionRedirectUri("com.okta.dev-6974382:/logout")
                .scopes("openid", "profile", "offline_access")
                .discoveryUri("https://dev-6974382.okta.com")
                .create()

        webAuth = Okta.WebAuthBuilder()
                .withConfig(oidcConfig)
                .withContext(applicationContext)
                .withStorage(SharedPreferenceStorage(this))
                .setRequireHardwareBackedKeyStore(false)
                .create()
        sessionClient = webAuth.sessionClient
    }

    private fun setupWebAuthCallback(webAuth: WebAuthClient) { // <4>
        val callback: ResultCallback<AuthorizationStatus, AuthorizationException> =
                object : ResultCallback<AuthorizationStatus, AuthorizationException> {
                    override fun onSuccess(status: AuthorizationStatus) {
                        if (status == AuthorizationStatus.AUTHORIZED) {
                            Log.d("MainActivity", "AUTHORIZED")
                            Toast.makeText(this@MainActivity, "Authorized", Toast.LENGTH_SHORT).show()
                        } else if (status == AuthorizationStatus.SIGNED_OUT) {
                            Log.d("MainActivity", "SIGNED_OUT")
                            Toast.makeText(this@MainActivity, "Signed out", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancel() {
                        Log.d("MainActivity", "CANCELED")
                        Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(msg: String?, error: AuthorizationException?) {
                        Log.d("MainActivity", "${error?.error} onError", error)
                        Toast.makeText(this@MainActivity, error?.toJsonString(), Toast.LENGTH_SHORT).show()
                    }
                }
        webAuth.registerCallback(callback, this)
    }
}

