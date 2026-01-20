package com.example.localhostapp

import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.examplewebviewloader.R
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    // Store the IP globally so the WebViewClient can access it
    private var activeIp: String = ""

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<View>(R.id.rootLayout)
        val button = findViewById<Button>(R.id.openWebViewButton)
        val webView = findViewById<WebView>(R.id.webView)
        val userIdInput = findViewById<EditText>(R.id.userIdInput)
        val ipInput = findViewById<EditText>(R.id.ipAddressInput)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                val method = request.method

                // Use the activeIp variable dynamically
                val finishUrl = "http://$activeIp:4000/finish-session"

                if (url == finishUrl && method == "POST") {
                    runOnUiThread {
                        webView.visibility = View.GONE
                        button.visibility = View.VISIBLE
                        userIdInput.visibility = View.VISIBLE
                        ipInput.visibility = View.VISIBLE // Show IP input again
                        webView.loadUrl("about:blank")
                    }

                    return WebResourceResponse(
                        "application/json",
                        "UTF-8",
                        200,
                        "OK",
                        mapOf("Access-Control-Allow-Origin" to "*"),
                        null
                    )
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        button.setOnClickListener {
            val userId = userIdInput.text.toString().trim()
            val ipAddress = ipInput.text.toString().trim()

            if (ipAddress.isEmpty()) {
                ipInput.error = "IP Address required"
                return@setOnClickListener
            }

            if (userId.isEmpty()) {
                userIdInput.error = "User ID required"
                return@setOnClickListener
            }

            // Update the state
            activeIp = ipAddress
            hideKeyboard()

            button.visibility = View.GONE
            userIdInput.visibility = View.GONE
            ipInput.visibility = View.GONE
            webView.visibility = View.VISIBLE

            // Build the URL dynamically
            val url = "http://$activeIp:4173/setup?userId=$userId"
            webView.loadUrl(url)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if the WebView is currently visible
                if (webView.visibility == View.VISIBLE) {
                    // Instead of exiting, hide the WebView and show the login fields
                    webView.visibility = View.GONE
                    webView.loadUrl("about:blank") // Stop any playing media/scripts

                    button.visibility = View.VISIBLE
                    userIdInput.visibility = View.VISIBLE
                    ipInput.visibility = View.VISIBLE
                } else {
                    // If WebView isn't showing, disable this callback and let the system exit
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        // Register the callback
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}