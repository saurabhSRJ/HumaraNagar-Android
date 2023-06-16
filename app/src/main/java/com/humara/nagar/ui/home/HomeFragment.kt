package com.humara.nagar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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
import com.humara.nagar.utils.GlideUtil
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment(), FeedItemClickListener {
    companion object {
        const val RELOAD_FEED = "reload_feed"
        const val UPDATE_POST = "post_id"
        const val DELETE_POST = "delete_post"
    }

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val postAdapter: PostAdapter by lazy {
        PostAdapter(requireContext(), this)
    }
    private val navController: NavController by lazy {
        findNavController()
    }
    private val homeViewModel: HomeViewModel by viewModels {
        ViewModelFactory()
    }
    private lateinit var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController.currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<Boolean>(RELOAD_FEED).observe(viewLifecycleOwner) { shouldReload ->
                if (shouldReload) {
                    reloadFeed()
                    requireContext().showToast("Post created successfully")
                    // To handle a result only once, you must call remove() on the SavedStateHandle to clear the result.
                    // If you do not remove the result, the LiveData will be triggered each time we come back to this fragment
                    remove<Boolean>(RELOAD_FEED)
                }
            }
            getLiveData<Long>(UPDATE_POST).observe(viewLifecycleOwner) { id ->
                homeViewModel.setPostUpdateRequired(id)
                remove<Long>(UPDATE_POST)
            }
            getLiveData<Long>(DELETE_POST).observe(viewLifecycleOwner) { id ->
                postAdapter.deletePost(id)
                remove<Long>(DELETE_POST)
            }
        }
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
            }
            initialPostProgressLiveData.observe(viewLifecycleOwner) {
                handleInitialProgress(it)
            }
            loadMorePostProgressLiveData.observe(viewLifecycleOwner) { progress ->
                if (progress) {
                    showPaginationLoader()
                } else {
                    hidePaginationLoader()
                }
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
            getUserPreference().profileImage?.let { url ->
                Glide.with(this@HomeFragment)
                    .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                    .transform(CenterCrop(), RoundedCorners(12))
                    .placeholder(R.drawable.ic_user_image_placeholder)
                    .error(R.drawable.ic_user_image_placeholder)
                    .into(ivProfilePhoto)
            }
            rvPost.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = postAdapter
                endlessRecyclerViewScrollListener = object : EndlessRecyclerViewScrollListener(layoutManager!!, 2) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        if (homeViewModel.canLoadMoreData) {
                            homeViewModel.getPosts()
                        }
                    }
                }
                addOnScrollListener(endlessRecyclerViewScrollListener)
            }
            swipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    reloadFeed()
                    swipeRefresh.isRefreshing = false
                }
            }
            // Configure the refreshing colors
            swipeRefresh.setColorSchemeResources(R.color.primary_color, R.color.stroke_green, R.color.stroke_yellow, R.color.stroke_red)
            binding.paginationLoader.retry.setNonDuplicateClickListener {
                homeViewModel.getPosts()
            }
            fabCreatePost.setNonDuplicateClickListener {
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToCreatePostFragment())
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
        val action = HomeFragmentDirections.actionHomeFragmentToCreatePostFragment(true, post.postId)
        navController.navigate(action)
    }

    override fun onDeletePostClick(post: Post) {
        FeedUtils.showDeletePostConfirmationDialog(parentFragmentManager, requireContext()) {
            homeViewModel.deletePost(post.postId)
        }
    }

    private fun reloadFeed() {
        endlessRecyclerViewScrollListener.resetState()
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