package com.humara.nagar.ui.report.complaint_status

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColorStateList
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.humara.nagar.R
import com.humara.nagar.Role
import com.humara.nagar.adapter.ComplaintStatusAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.ComplaintShareLayoutBinding
import com.humara.nagar.databinding.FragmentComplaintStatusBinding
import com.humara.nagar.ui.report.complaints.ComplaintManagementViewModel
import com.humara.nagar.ui.report.model.ComplaintStatus
import com.humara.nagar.ui.report.model.UpdateComplaintRequest
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
    private val complaintManagementViewModel by navGraphViewModels<ComplaintManagementViewModel>(R.id.navigation_report) {
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
            val comment = bundle.parcelable<UpdateComplaintRequest>(COMMENT_KEY)!!
            if (Role.isFromHumaraNagarTeam(getUserPreference().role?.id ?: 0)) {
                context?.showToast("Action not allowed")
                return@setFragmentResultListener
            }
            complaintStatusViewModel.onUserCommentReceived(comment, Role.isLocalAdmin(getUserPreference().role?.id ?: 0))
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
                getComplaintStatus(args.complaintId)
            }
            acknowledgementErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            finishComplaintSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_resolved))
                setComplaintsListReload()
                getComplaintStatus(args.complaintId)
            }
            finishComplaintErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            withdrawSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_successfully_withdrawn))
                setComplaintsListReload()
                navController.navigateUp()
            }
            withdrawErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            ratingSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.thank_you_for_the_rating))
                showRatingSubmittedUI()
                setComplaintsListReload()
            }
            ratingErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
                binding.ratingBar.rating = 0F
            }
        }
    }

    private fun setComplaintsListReload() {
        complaintManagementViewModel.setReloadComplaintsList()
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleComplaintStatusUI(response: ComplaintStatus) {
        binding.run {
            nsvMain.visibility = View.VISIBLE
            categoryTV.setVisibilityAndText(response.category)
            tvWard.setVisibilityAndText(getString(R.string.ward_s, response.ward))
            locationTV.setVisibilityAndText(response.location)
            if (Role.isAdmin(getUserPreference().role?.id ?: 0)) {
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
                imageView.loadUrl(images[0], R.drawable.ic_image_placeholder)
                val shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.wobble)
                imageContainer.startAnimation(shakeAnimation)
                imageView.setNonDuplicateClickListener {
                    showImagePreviewFragment(images)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder)
            }
            response.profileImage?.let { url ->
                complaintInitiatorIV.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
            callComplaintInitiatorCard.setNonDuplicateClickListener {
                response.phoneNumber?.let { number ->
                    context?.startActivity(IntentUtils.getCallIntent(number))
                }
            }
            locationTV.setNonDuplicateClickListener {
                if (response.latitude != null && response.longitude != null) {
                    val mapIntent = IntentUtils.getGoogleMapIntent(response.latitude.toDouble(), response.longitude.toDouble())
                    context?.startActivity(mapIntent)
                } else {
                    context?.showToast(getString(R.string.map_location_not_available_message))
                }
            }
            if (Role.isLocalAdmin(getUserPreference().role?.id ?: 0)) {
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
        binding.run {
            //Setting up the top app bar title
            includedToolbar.apply {
                toolbarTitle.text = resources.getString(R.string.complaint_status)
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
                rightIcon.visibility = View.VISIBLE
                rightIconIv.setImageResource(R.drawable.ic_share_complaint)
                rightIconTv.text = getString(R.string.share)
                rightIcon.setNonDuplicateClickListener {
                    shareComplaint()
                }
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
        val action = ComplaintStatusFragmentDirections.actionComplaintStatusFragmentToComplaintStatusUpdateDialogFragment(
            complaintStatusViewModel.complaintStatusLiveData.value!!.currentState,
            getScreenName()
        )
        navController.navigate(action)
    }

    private fun handleAdminResponse(response: ComplaintStatus) {
        binding.run {
            if (response.showRatingSection()) {
                buttonCTA.visibility = View.GONE
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

    private fun shareComplaint() {
        showProgress(true)
        inflateShareComplaintLayout()
    }

    private fun inflateShareComplaintLayout() {
        val complaintShareBinding = ComplaintShareLayoutBinding.inflate(layoutInflater, binding.root as ViewGroup, true)
        complaintStatusViewModel.complaintStatusLiveData.value?.let { complaint ->
            complaintShareBinding.run {
                categoryTV.text = complaint.category
                tvWard.setVisibilityAndText(getString(R.string.ward_s, complaint.ward))
                tvResidentName.setVisibilityAndText(complaint.residentName)
                tvComplaintId.text = getString(R.string.complaint_id_n, args.complaintId)
                tvComment.text = complaint.comments
                stateBtn.apply {
                    text = ComplaintsUtils.ComplaintState.getName(complaint.currentState, context)
                    val stateColor = ComplaintsUtils.ComplaintState.getStateColor(complaint.currentState)
                    setTextColor(ContextCompat.getColor(context, stateColor))
                    (this as? MaterialButton)?.setStrokeColorResource(stateColor)
                }
                complaint.images?.let { url ->
                    Glide.with(requireContext())
                        .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                shareComplaintViaIntent(complaintShareBinding.root, complaint)
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                shareComplaintViaIntent(complaintShareBinding.root, complaint)
                                return false
                            }
                        })
                        .into(imageView)
                } ?: run {
                    shareComplaintViaIntent(complaintShareBinding.root, complaint)
                }
            }
        }
    }

    private fun shareComplaintViaIntent(complaintShareView: View, complaintStatus: ComplaintStatus) {
        complaintShareView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                complaintShareView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val shareComplaintViaWhatsAppBitmap = Bitmap.createBitmap(
                    complaintShareView.measuredWidth,
                    complaintShareView.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val c = Canvas(shareComplaintViaWhatsAppBitmap!!)
                complaintShareView.draw(c)
                IntentUtils.shareViaIntent(
                    requireActivity(),
                    shareComplaintViaWhatsAppBitmap,
                    getString(R.string.share_complaint_caption, complaintStatus.residentName, complaintStatus.phoneNumber, "https://humara.nagar/grievance/${args.complaintId}/send")
                )
                (binding.root as ViewGroup).removeView(complaintShareView)
                hideProgress()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.COMPLAINTS_STATUS_FRAGMENT
}