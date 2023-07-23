package com.humara.nagar.ui.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.humara.nagar.KohiiProvider
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.FeedFiltersAdapter
import com.humara.nagar.adapter.FeedItemClickListener
import com.humara.nagar.adapter.PollOptionsPreviewAdapter
import com.humara.nagar.adapter.PostAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentHomeBinding
import com.humara.nagar.databinding.PostShareLayoutBinding
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.common.EndlessRecyclerViewScrollListener
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.ui.report.ReportFragment
import com.humara.nagar.utils.*
import kohii.v1.core.MemoryMode
import kohii.v1.exoplayer.Kohii
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
        PostAdapter(kohii, requireContext(), this)
    }
    private val navController: NavController by lazy {
        findNavController()
    }
    private val homeViewModel: HomeViewModel by viewModels {
        ViewModelFactory()
    }
    private val filterAdapter: FeedFiltersAdapter by lazy {
        FeedFiltersAdapter { filter, position ->
            binding.rvFilters.smoothScrollToPosition(position)
            if (homeViewModel.selectedFilterLiveData.value == filter.id) {
                binding.rvPost.smoothScrollToPosition(0)
            } else {
                homeViewModel.setFilterSelection(filter.id)
                reloadFeed()
            }
        }
    }
    private lateinit var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener
    private lateinit var kohii: Kohii

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
        setUpVideoPlayer()
        initViewModelAndObservers()
        initView()
    }

    private fun setUpVideoPlayer() {
        kohii = KohiiProvider.get(requireContext())
        kohii.register(this, memoryMode = MemoryMode.HIGH)
            .addBucket(binding.rvPost)
    }

    private fun initViewModelAndObservers() {
        homeViewModel.run {
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            observeProgress(this)
            initialPostsLiveData.observe(viewLifecycleOwner) {
                binding.rvPost.smoothScrollToPosition(0)
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
                showErrorDialog(subtitle = it.message, ctaText = getString(R.string.retry), errorAction = { reloadFeed() }, dismissAction = { })
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
            feedFiltersLiveData.observe(viewLifecycleOwner) {
                filterAdapter.setData(it)
            }
        }
    }

    private fun initView() {
        binding.run {
            getUserPreference().profileImage?.let { url ->
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
            ivProfilePhoto.setNonDuplicateClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment2(getScreenName())
                navController.navigate(action)
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
                setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    val isVisible = scrollY <= oldScrollY
                    showHideBottomNavigationView(isVisible)
                }
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
            rvFilters.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = filterAdapter
                setHasFixedSize(true)
            }
        }
    }

    private fun showHideBottomNavigationView(show: Boolean) {
        (activity as? BaseActivity)?.findViewById<BottomNavigationView>(R.id.nav_view)?.isVisible = show
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
        binding.paginationLoader.retry.visibility = View.GONE
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
        binding.paginationLoader.apply {
            progress.visibility = View.GONE
            retry.visibility = View.GONE
        }
    }

    private fun showPaginationLoadError() {
        binding.paginationLoader.retry.visibility = View.VISIBLE
    }

    override fun onSharePostClick(post: Post) {
        (activity as BaseActivity).requestPermissions(PermissionUtils.sharePostPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(ReportFragment.TAG, "fragment detached from the activity")
                    return
                }
                inflateSharePostLayout(post)
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun inflateSharePostLayout(post: Post) {
        showProgress(true)
        val postShareBinding = PostShareLayoutBinding.inflate(layoutInflater, binding.root as ViewGroup, true)
        postShareBinding.run {
            tvName.text = post.name
            tvRoleAndWard.text = FeedUtils.getRoleAndWardText(requireContext(), post.role, post.ward)
            postContent.setVisibilityAndText(post.caption)
            post.profileImage?.let {
                Glide.with(requireContext())
                    .load(GlideUtil.getUrlWithHeaders(post.profileImage, requireContext()))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            addPostTypeDataBeforeSharing(post, postShareBinding)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            addPostTypeDataBeforeSharing(post, postShareBinding)
                            return false
                        }
                    })
                    .into(ivProfilePhoto)
            } ?: run {
                addPostTypeDataBeforeSharing(post, postShareBinding)
            }
        }
    }

    private fun addPostTypeDataBeforeSharing(post: Post, binding: PostShareLayoutBinding) {
        when (post.type) {
            PostType.IMAGE.type -> addImagePostShareData(post, binding)
            PostType.POLL.type -> addPollPostShareData(post, binding)
            PostType.VIDEO.type -> addVideoPostShareData(post, binding)
            PostType.DOCUMENT.type, PostType.TEXT.type -> sharePostOnWhatsapp(binding.root, post.name, post.postId)
            else -> {}
        }
    }

    private fun addImagePostShareData(post: Post, binding: PostShareLayoutBinding) {
        val url = post.info?.mediaDetails?.getOrNull(0)?.media
        url?.let {
            binding.ivPostImage.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                .placeholder(R.drawable.ic_image_placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        sharePostOnWhatsapp(binding.root, post.name, post.postId)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        sharePostOnWhatsapp(binding.root, post.name, post.postId)
                        return false
                    }
                })
                .into(binding.ivPostImage)
        } ?: {
            sharePostOnWhatsapp(binding.root, post.name, post.postId)
        }
    }

    private fun addPollPostShareData(post: Post, binding: PostShareLayoutBinding) {
        binding.pollLayout.run {
            root.visibility = View.VISIBLE
            post.info?.let {
                tvQuestion.text = it.question
                tvSubTitle.text = resources.getQuantityString(R.plurals.n_votes, it.totalVotes, it.totalVotes)
                rvOptions.apply {
                    adapter = PollOptionsPreviewAdapter(it.getOptionsText())
                    setHasFixedSize(true)
                }
                if (it.isExpired()) {
                    tvExpiryTime.text = getString(R.string.completed)
                    tvExpiryTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.stroke_green))
                } else {
                    tvExpiryTime.text = DateTimeUtils.getRemainingDurationForPoll(requireContext(), it.expiryTime)
                    tvExpiryTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_585C60))
                }
            }
            sharePostOnWhatsapp(binding.root, post.name, post.postId)
        }
    }

    private fun addVideoPostShareData(post: Post, binding: PostShareLayoutBinding) {
        binding.videoThumbnail.run {
            val url = post.info?.mediaDetails?.getOrNull(0)?.thumbnailUrl
            url?.let {
                root.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            sharePostOnWhatsapp(binding.root, post.name, post.postId)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            sharePostOnWhatsapp(binding.root, post.name, post.postId)
                            return false
                        }
                    })
                    .into(ivThumbnail)
            } ?: run {
                sharePostOnWhatsapp(binding.root, post.name, post.postId)
            }
        }
    }

    private fun sharePostOnWhatsapp(postShareView: View, name: String, postId: Long) {
        postShareView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                postShareView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val shareProfileViaWhatsAppBitmap = Bitmap.createBitmap(
                    postShareView.measuredWidth,
                    postShareView.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val c = Canvas(shareProfileViaWhatsAppBitmap!!)
                postShareView.draw(c)
                IntentUtils.shareViaIntent(
                    requireActivity(),
                    shareProfileViaWhatsAppBitmap,
                    getString(R.string.share_post_caption, name, "https://humara.nagar/post/${postId}/send")
                )
                (binding.root as ViewGroup).removeView(postShareView)
                hideProgress()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.HOME_FRAGMENT
}