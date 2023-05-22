package com.humara.nagar.ui.report.complaint_status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getColorStateList
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.humara.nagar.R
import com.humara.nagar.adapter.ComplaintStatusAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentComplaintStatusBinding
import com.humara.nagar.ui.report.complaints.ComplaintsFragment
import com.humara.nagar.ui.report.model.ComplaintStatus
import com.humara.nagar.utils.*

class ComplaintStatusFragment : BaseFragment() {
    companion object {
        const val COMMENT_KEY = "comment"
        const val COMMENT_RESULT_REQUEST = "COMMENT_RESULT_REQUEST"
    }

    private var _binding: FragmentComplaintStatusBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val complaintStatusViewModel by viewModels<ComplaintStatusViewModel> {
        ViewModelFactory()
    }
    private val navController: NavController by lazy { findNavController() }
    private val complaintStatusAdapter: ComplaintStatusAdapter by lazy {
        ComplaintStatusAdapter()
    }
    private val args: ComplaintStatusFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComplaintStatusBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
        // result listener for getting the comment from dialog fragment
        setFragmentResultListener(COMMENT_RESULT_REQUEST) { _, bundle ->
            val comment = StringUtils.replaceWhitespaces(bundle.getString(COMMENT_KEY) ?: "")
            complaintStatusViewModel.onUserCommentReceived(comment, getUserPreference().isAdminUser)
        }
    }

    private fun initViewModelObservers() {
        complaintStatusViewModel.run {
            observeProgress(this, false)
            observeException(this)
            ratingData.observe(viewLifecycleOwner) {
                binding.ratingBar.rating = it.toFloat()
            }
            complaintStatusLiveData.observe(viewLifecycleOwner) {
                handleComplaintStatusUI(it)
            }
            complaintStatusErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog()
            }
            acknowledgementSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_acknowledgment_sent))
                setComplaintsListReload()
                navController.navigateUp()
            }
            acknowledgementErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(errorAction = {}, dismissAction = {})
            }
            finishComplaintSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_resolved))
                setComplaintsListReload()
                navController.navigateUp()
            }
            finishComplaintErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(errorAction = {}, dismissAction = {})
            }
            withdrawSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_successfully_withdrawn))
                setComplaintsListReload()
                navController.navigateUp()
            }
            withdrawErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(errorAction = {}, dismissAction = {})
            }
            ratingSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.thank_you_for_the_rating))
                showRatingSubmittedUI()
                setComplaintsListReload()
            }
            ratingErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(errorAction = {}, dismissAction = {})
                binding.ratingBar.rating = 0F
            }
        }
    }

    private fun setComplaintsListReload() {
        navController.previousBackStackEntry?.savedStateHandle?.set(ComplaintsFragment.RELOAD_COMPLAINTS_LIST, true)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleComplaintStatusUI(response: ComplaintStatus) {
        binding.run {
            nsvMain.visibility = View.VISIBLE
            categoryTV.setVisibilityAndText(response.category)
            localityTV.setVisibilityAndText(response.locality)
            locationTV.setVisibilityAndText(response.location)
            if (getUserPreference().isAdminUser) {
                response.residentName?.let { name ->
                    complaintInitiatorDetailsLayout.visibility = View.VISIBLE
                    complaintInitiatorNameTV.text = name
                }
            }
            descriptionTV.setVisibilityAndText(response.comments)
            complaintIdTV.text = args.complaintId
            complaintStatusAdapter.setData(response.trackingInfo)
            val images = StringUtils.convertToList(response.images)
            if (images.isNotEmpty()) {
                Glide.with(this@ComplaintStatusFragment)
                    .load(GlideUtil.getUrlWithHeaders(images[0], root.context))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transform(CenterCrop(), RoundedCorners(12))
                    .transition(DrawableTransitionOptions.withCrossFade(1000))
                    .into(imageView)
                val shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.wobble)
                imageContainer.startAnimation(shakeAnimation)
                imageView.setNonDuplicateClickListener {
                    showImagePreviewFragment(images)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder)
            }
            callComplaintInitiatorCard.setNonDuplicateClickListener {
                response.phoneNumber?.let { number ->
                    context?.startActivity(IntentUtils.getCallIntent(number))
                }
            }
            if (response.latitude != null && response.longitude != null) {
                val mapIntent = IntentUtils.getGoogleMapIntent(response.latitude.toDouble(), response.longitude.toDouble())
                if (IntentUtils.hasIntent(requireContext(), mapIntent)) {
                    locationTV.setNonDuplicateClickListener {
                        context?.startActivity(mapIntent)
                    }
                }
            } else {
                context?.showToast(getString(R.string.map_location_not_available_message))
            }
            if (getUserPreference().isAdminUser) {
                handleAdminResponse(response)
            } else {
                handleUserResponse(response)
            }
        }
    }

    private fun showImagePreviewFragment(images: List<String>) {
        val action = ComplaintStatusFragmentDirections.actionComplaintStatusToImagePreview(
            images.toTypedArray(),
            getScreenName()
        )
        navController.navigate(action)
    }

    private fun initView() {
        binding.apply {
            //Setting up the top app bar title
            includedToolbar.toolbarTitle.text = resources.getString(R.string.complaint_status)
            includedToolbar.leftIcon.setOnClickListener {
                navController.navigateUp()
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = complaintStatusAdapter
            }
            buttonCTA.setOnClickListener {
                showComplaintStatusUpdateDialog()
            }
        }
    }

    private fun showComplaintStatusUpdateDialog() {
        val action = ComplaintStatusFragmentDirections.actionComplaintStatusFragmentToComplaintStatusUpdateDialogFragment(getScreenName())
        navController.navigate(action)
    }

    private fun handleAdminResponse(response: ComplaintStatus) {
        binding.run {
            if (response.showRatingSection()) {
                ratingBar.apply {
                    visibility = View.VISIBLE
                    setIsIndicator(true)
                }
                rateThisServiceTV.visibility = View.VISIBLE
                rateThisServiceTV.text = if (complaintStatusViewModel.rating > 0) getString(R.string.rating_for_this_service) else getString(R.string.no_rating_received)
            } else {
                buttonCTA.visibility = View.VISIBLE
                if (response.currentState == ComplaintsUtils.ComplaintState.IN_PROGRESS.currentState) {
                    buttonCTA.text = getString(R.string.finish)
                    buttonCTA.backgroundTintList = getColorStateList(requireContext(), R.color.stroke_green)
                } else {
                    buttonCTA.text = getString(R.string.acknowledge)
                }
            }
        }
    }

    private fun handleUserResponse(response: ComplaintStatus) {
        binding.run {
            if (response.showRatingSection()) {
                if (complaintStatusViewModel.rating > 0) {
                    showRatingSubmittedUI()
                } else {
                    rateThisServiceTV.text = getString(R.string.rate_this_service)
                    ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                        if (rating.toInt() != 0) {
                            complaintStatusViewModel.updateRatingData(rating.toInt())
                            //TODO: show a rating dialog instead of generic comment dialog
                            showComplaintStatusUpdateDialog()
                        }
                    }
                }
                rateThisServiceTV.visibility = View.VISIBLE
                ratingBar.visibility = View.VISIBLE
            } else {
                buttonCTA.visibility = View.VISIBLE
                buttonCTA.text = getString(R.string.withdraw)
            }
        }
    }

    private fun showRatingSubmittedUI() {
        binding.run {
            ratingBar.setIsIndicator(true)
            rateThisServiceTV.text = getString(R.string.you_rated_this_service)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.COMPLAINTS_STATUS_FRAGMENT
}