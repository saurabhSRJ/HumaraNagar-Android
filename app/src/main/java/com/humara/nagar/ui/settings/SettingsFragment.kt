package com.humara.nagar.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.humara.nagar.BuildConfig
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.databinding.FragmentSettingsBinding
import com.humara.nagar.utils.loadUrl
import com.humara.nagar.utils.setNonDuplicateClickListener

class SettingsFragment : BaseFragment() {
    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                toolbarTitle.text = getString(R.string.settings)
                leftIcon.setOnClickListener { navController.navigateUp() }
            }
            getUserPreference().profileImage?.let { url ->
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
            tvName.text = getUserPreference().userProfile?.name
            languageItem.apply {
                tvTitle.text = getString(R.string.language)
                root.setNonDuplicateClickListener {
                    navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToSelectLanguageBottomSheet(getScreenName()))
                }
            }
            aboutUsItem.apply {
                tvTitle.text = getString(R.string.about_us)
                root.setNonDuplicateClickListener {
                    openWebView(NetworkConstants.NetworkAPIConstants.ABOUT_US_URL, getString(R.string.about_us))
                }
            }
            privacyPolicyItem.apply {
                tvTitle.text = getString(R.string.privacy_policy)
                root.setNonDuplicateClickListener {
                    openWebView(NetworkConstants.NetworkAPIConstants.PRIVACY_POLICY_URL, getString(R.string.privacy_policy))
                }
            }
            termsItem.apply {
                tvTitle.text = getString(R.string.terms_amp_conditions)
                root.setNonDuplicateClickListener {
                    openWebView(NetworkConstants.NetworkAPIConstants.TERMS_CONDITION_URL, getString(R.string.terms_amp_conditions))
                }
            }
            logoutItem.apply {
                tvTitle.text = getString(R.string.logout)
                root.setOnClickListener {
                    navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToLogoutBottomSheet(getScreenName()))
                }
            }
            tvAppVersion.text = getString(R.string.app_version_d, BuildConfig.VERSION_NAME)
        }
    }

    private fun openWebView(url: String, title: String) {
        val action = SettingsFragmentDirections.actionSettingsFragmentToWebViewFragment(url, getScreenName(), title)
        navController.navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.SETTINGS_FRAGMENT
}