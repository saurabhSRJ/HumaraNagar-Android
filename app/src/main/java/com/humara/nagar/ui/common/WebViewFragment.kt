package com.humara.nagar.ui.common

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.FragmentWebViewBinding

/**
 * Activity that displays a WebView to load and display web content.
 */
class WebViewFragment : BaseFragment() {
    private lateinit var binding: FragmentWebViewBinding

    companion object {
        const val TAG = "WebViewFragment"
        private const val URL = "url"
        private const val TITLE = "title"
        fun getInstance(source: String, url: String, title: String) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(IntentKeyConstants.SOURCE, source)
                putString(URL, url)
                putString(TITLE, title)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(URL)
        val title = arguments?.getString(TITLE)

        binding.toolbar.apply {
            toolbarTitle.text = title
            leftIcon.setOnClickListener { requireActivity().onBackPressed() }
        }

        binding.webView.apply {
            webViewClient = CustomWebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            url?.let { loadUrl(it) }
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