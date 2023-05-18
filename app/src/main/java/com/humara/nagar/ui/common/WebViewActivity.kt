package com.humara.nagar.ui.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.ActivityWebViewBinding

/**
 * Activity that displays a WebView to load and display web content.
 */
class WebViewActivity : BaseActivity() {
    private lateinit var binding: ActivityWebViewBinding
    companion object {
        private const val URL = "url"
        private const val TITLE = "title"
        fun startActivity(context: Context, source: String, url: String, title: String) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(IntentKeyConstants.SOURCE, source)
                putExtra(URL, url)
                putExtra(TITLE, title)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.run {
            toolbar.apply {
                toolbarTitle.text = intent?.getStringExtra(TITLE)
                leftIcon.setOnClickListener { finish() }
            }
            webView.apply {
                webViewClient = CustomWebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                intent?.getStringExtra(URL)?.let { loadUrl(it) }
            }
        }
    }

    inner class CustomWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun getScreenName(): String = AnalyticsData.ScreenName.WEB_VIEW_ACTIVITY
}