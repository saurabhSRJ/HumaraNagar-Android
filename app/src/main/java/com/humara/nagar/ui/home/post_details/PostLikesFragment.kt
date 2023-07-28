package com.humara.nagar.ui.home.post_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humara.nagar.R
import com.humara.nagar.adapter.PostLikesAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentPostLikesBinding

class PostLikesFragment : BaseFragment() {
    private lateinit var binding: FragmentPostLikesBinding
    private val postLikesAdapter: PostLikesAdapter by lazy {
        PostLikesAdapter()
    }
    private val navController: NavController by lazy {
        findNavController()
    }
    private val postLikesViewModel: PostLikesViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPostLikesBinding.inflate(inflater, container, false)
        initViewModelObservers()
        initView()
        return binding.root
    }

    private fun initViewModelObservers() {
        postLikesViewModel.run {
            observeProgress(this)
            observeErrorAndException(this)
            postLikesLiveData.observe(viewLifecycleOwner) {
                postLikesAdapter.setData(it)
            }
        }
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                toolbarTitle.text = getString(R.string.liked_by)
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
            }
            rvLikes.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = postLikesAdapter
            }
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.POST_LIKES_FRAGMENT
}