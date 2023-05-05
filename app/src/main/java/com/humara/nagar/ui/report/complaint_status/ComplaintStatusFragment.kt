package com.humara.nagar.ui.report.complaint_status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColorStateList
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
            observerException(this)
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
                findNavController().navigateUp()
            }
            acknowledgementErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(errorAction = {}, dismissAction = {})
            }
            finishComplaintSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_resolved))
                setComplaintsListReload()
                findNavController().navigateUp()
            }
            finishComplaintErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(errorAction = {}, dismissAction = {})
            }
            withdrawSuccessLiveData.observe(viewLifecycleOwner) {
                showSnackBar(getString(R.string.complaint_successfully_withdrawn))
                setComplaintsListReload()
                findNavController().navigateUp()
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
        findNavController().previousBackStackEntry?.savedStateHandle?.set(ComplaintsFragment.RELOAD_COMPLAINTS_LIST, true)
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
            response.trackingInfo?.let {
                complaintStatusAdapter.setData(it.states)
            }
            if (getUserPreference().isAdminUser) {
                handleAdminResponse(response)
            } else {
                handleUserResponse(response)
            }
        }
    }

    private fun initView() {
        binding.apply {
            //Setting up the top app bar title
            includedToolbar.toolbarTitle.text = resources.getString(R.string.complaint_status)
            includedToolbar.leftIcon.setOnClickListener {
                findNavController().navigateUp()
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = complaintStatusAdapter
            }
            imageView.setOnClickListener {
                complaintStatusViewModel.imageListData.value?.let { images ->
                    if (images.isNotEmpty()) {
                        val action = ComplaintStatusFragmentDirections.actionComplaintStatusToImagePreview(
                            images.toTypedArray()
                        )
                        findNavController().navigate(action)
                    }
                }
            }
            buttonCTA.setOnClickListener {
                showComplaintStatusUpdateDialog()
            }
            callComplaintInitiatorCard.setNonDuplicateClickListener {
                complaintStatusViewModel.complaintStatusLiveData.value?.phoneNumber?.let { number ->
                    context?.startActivity(IntentUtils.getCallIntent(number))
                }
            }
        }
    }

    private fun showComplaintStatusUpdateDialog() {
        val action = ComplaintStatusFragmentDirections.actionComplaintStatusFragmentToComplaintStatusUpdateDialogFragment()
        findNavController().navigate(action)
    }

    private fun handleAdminResponse(response: ComplaintStatus) {
        binding.run {
            if (response.showRatingSection()) {
                ratingBar.apply {
                    visibility = View.VISIBLE
                    setIsIndicator(true)
                }
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