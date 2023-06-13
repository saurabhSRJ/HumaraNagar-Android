package com.humara.nagar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.adapter.FeedItemClickListener
import com.humara.nagar.adapter.PostAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentHomeBinding
import com.humara.nagar.ui.common.EndlessRecyclerViewScrollListener
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.utils.FeedUtils
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment(), FeedItemClickListener {
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val postAdapter: PostAdapter by lazy {
        PostAdapter(requireContext(), this)
    }
    private val navController: NavController by lazy {
        findNavController()
    }
    private val homeViewModel: HomeViewModel by navGraphViewModels(R.id.home_navigation) {
        ViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelAndObservers()
        initView()
    }

    private fun initViewModelAndObservers() {
        homeViewModel.run {
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            observeProgress(this)
            initialPostsLiveData.observe(viewLifecycleOwner) {
                postAdapter.setData(it)
            }
            loadMorePostsLiveData.observe(viewLifecycleOwner) {
                postAdapter.addMoreData(it)
                hidePaginationLoader()
            }
            initialPostProgressLiveData.observe(viewLifecycleOwner) {
                handleInitialProgress(it)
            }
            initialPostErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, ctaText = getString(R.string.retry), errorAction = { reloadFeed() }, dismissAction = { reloadFeed() })
            }
            loadMorePostErrorLiveData.observe(viewLifecycleOwner) {
                showPaginationLoadError()
            }
            updatePostLiveData.observe(viewLifecycleOwner) { id ->
                getPostDetails(id)
            }
            likePostErrorLiveData.observe(viewLifecycleOwner) { id ->
                getPostDetails(id)
                requireContext().showToast(getString(R.string.like_button_error_message), true)
            }
            voteSuccessLiveData.observe(viewLifecycleOwner) { post ->
                postAdapter.updatePost(post)
            }
            voteErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            deletePostLiveData.observe(viewLifecycleOwner) { id ->
                postAdapter.deletePost(id)
            }
            postDetailsLiveData.observe(viewLifecycleOwner) {
                postAdapter.updatePost(it)
            }
        }
    }

    private fun initView() {
        binding.run {
            ivNotification.setNonDuplicateClickListener {
            }
            rvPost.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = postAdapter
                val scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager!!, 2) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        if (homeViewModel.canLoadMoreData) {
                            homeViewModel.getPosts()
                            showPaginationLoader()
                        }
                    }
                }
                addOnScrollListener(scrollListener)
            }
            swipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    reloadFeed()
                    swipeRefresh.isRefreshing = false
                }
            }
            // Configure the refreshing colors
            swipeRefresh.setColorSchemeResources(R.color.blue_4285F4, R.color.stroke_green, R.color.stroke_yellow, R.color.stroke_red)
            binding.paginationLoader.retry.setNonDuplicateClickListener {
                homeViewModel.getPosts()
            }
        }
    }

    override fun onLikeButtonClick(post: Post) {
        homeViewModel.flipUserLike(post)
    }

    override fun submitVote(post: Post, optionId: Int) {
        homeViewModel.submitVote(post.postId, optionId)
    }

    override fun onEditPostClick(post: Post) {
        TODO("Not yet implemented")
    }

    override fun onDeletePostClick(post: Post) {
        FeedUtils.showDeletePostConfirmationDialog(parentFragmentManager, requireContext()) {
            homeViewModel.deletePost(post.postId)
        }
    }

    private fun reloadFeed() {
        homeViewModel.resetPaginationState()
        homeViewModel.getPosts()
    }

    private fun handleInitialProgress(showProgress: Boolean) {
        if (showProgress) {
            binding.run {
                shimmerLayout.visibility = View.VISIBLE
                rvPost.visibility = View.GONE
                shimmerLayout.startShimmer()
            }
        } else {
            binding.run {
                shimmerLayout.visibility = View.GONE
                rvPost.visibility = View.VISIBLE
                shimmerLayout.stopShimmer()
            }
        }
    }

    private fun showPaginationLoader() {
        binding.paginationLoader.apply {
            progress.visibility = View.VISIBLE
            retry.visibility = View.GONE
        }
    }

    private fun hidePaginationLoader() {
        binding.paginationLoader.progress.visibility = View.GONE
    }

    private fun showPaginationLoadError() {
        binding.paginationLoader.retry.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.HOME_FRAGMENT
}