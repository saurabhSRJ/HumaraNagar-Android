package com.humara.nagar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humara.nagar.R
import com.humara.nagar.adapter.PostAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.databinding.FragmentHomeBinding
import com.humara.nagar.utils.setNonDuplicateClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val postAdapter: PostAdapter by lazy {
        PostAdapter(requireContext())
    }
    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.run {
            ivNotification.setNonDuplicateClickListener {
            }
            rvPost.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = postAdapter
            }
            swipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    delay(2000)
                    postAdapter.shuffleList()
                    swipeRefresh.isRefreshing = false
                }
            }
            // Configure the refreshing colors
            swipeRefresh.setColorSchemeResources(R.color.blue_4285F4,
                R.color.stroke_green,
                R.color.stroke_yellow,
                R.color.stroke_red);
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.HOME_FRAGMENT
}