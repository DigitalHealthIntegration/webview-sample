package com.example.localhostapp

import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.examplewebviewloader.R

class MainActivity : AppCompatActivity() {
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootLayout = findViewById<View>(R.id.rootLayout)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val button = findViewById<Button>(R.id.openWebViewButton)
        val webView = findViewById<WebView>(R.id.webView)
        val userIdInput = findViewById<EditText>(R.id.userIdInput)

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

                if (url == "http://10.102.10.193:4000/finish-session" && method == "POST") {
                    runOnUiThread {
                        // Go back to the start screen
                        webView.visibility = View.GONE
                        button.visibility = View.VISIBLE
                        userIdInput.visibility = View.VISIBLE

                        // Optional cleanup
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
            hideKeyboard()
            val userId = userIdInput.text.toString().trim()

            if (userId.isEmpty()) {
                userIdInput.error = "User ID required"
                return@setOnClickListener
            }

            button.visibility = View.GONE
            userIdInput.visibility = View.GONE
            webView.visibility = View.VISIBLE

            val url = "http://10.102.10.193:4173/setup?userId=$userId"
            webView.loadUrl(url)
        }
    }
}
