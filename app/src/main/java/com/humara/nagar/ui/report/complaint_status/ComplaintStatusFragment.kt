package com.humara.nagar.ui.report.complaint_status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.ComplaintStatusAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentComplaintStatusBinding
import com.humara.nagar.databinding.ToolbarLayoutBinding
import com.humara.nagar.utils.Utils


class ComplaintStatusFragment : BaseFragment(), AdminDialogFragment.DialogListener {

    private var _binding: FragmentComplaintStatusBinding? = null
    private val complaintStatusViewModel by viewModels<ComplaintStatusViewModel> {
        ViewModelFactory()
    }
    private lateinit var stepRecyclerView: RecyclerView
    private lateinit var complaintStatusAdapter: ComplaintStatusAdapter
    private val args: ComplaintStatusFragmentArgs by navArgs()
    private var complaintId: String? = null

    private var isUserAdmin = false
    private lateinit var toolbar: ToolbarLayoutBinding
    private val binding get() = _binding!!
    private var currentState = ""
    private val imageList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentComplaintStatusBinding.inflate(layoutInflater, container, false)

        initViewModelObservers()
        initView()

        binding.apply {

            buttonCTA.setOnClickListener {
                showCustomDialog()
            }

            imageView.setOnClickListener {
                val action = ComplaintStatusFragmentDirections.actionComplaintStatusFragmentToImagePreviewFragment(
                    imageList.toList().toTypedArray()
                )
                findNavController().navigate(action)
            }

            includedToolbar.leftIcon.setOnClickListener {
                findNavController().navigateUp()
            }

            callComplaintInitiatorCard.setOnClickListener {
                Utils.makeCallViaIntent(requireContext(), getUserPreference().mobileNumber)
            }

            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                Logger.debugLog("Rating bar result $rating")

                if (rating.toInt() != 0) {
                    complaintId?.let {
                        complaintStatusViewModel.postRatingRequest(
                            it,
                            rating.toInt()
                        )
                    }
                }
            }
        }

        return binding.root
    }

    private fun initView() {
        isUserAdmin = getUserPreference().isUserAdmin
        complaintId = args.complaintId

        binding.apply {
            //Setting up the top app bar title
            toolbar = includedToolbar
            toolbar.toolbarTitle.text = resources.getString(R.string.complaint_status)

            if (isUserAdmin) {
                complaintInitiatorLayout.visibility = View.VISIBLE
                buttonCTA.text = resources.getString(R.string.acknowledge)
                currentState = resources.getString(R.string.acknowledge)
            } else {
                currentState = resources.getString(R.string.withdraw)
            }

            //Set-up recyclerview and adapter
            stepRecyclerView = recyclerView
            complaintStatusAdapter = ComplaintStatusAdapter(requireContext())
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = complaintStatusAdapter
            }
        }

    }

    private fun initViewModelObservers() {
        complaintStatusViewModel.run {
            observeProgress(this, false)
            observerException(this)

            if (this.complaintStatusLiveData.value == null) {
                //Api call was never made
                getComplaintStatus()
            }
            complaintStatusLiveData.observe(viewLifecycleOwner) {

                binding.apply {

                    categoryTV.text = it.category
                    localityTV.text = it.locality
                    locationTV.text = it.location
                    complaintInitiatorNameTV.text = it.resident_name
                    descriptionTV.text = it.comments
                    complaintIdTV.text = args.complaintId
                    imageList.apply {
                        clear()
                        addAll(it.images)
                    }

                    it.trackingInfo?.let {
                        complaintStatusAdapter.addData(it.states)
                    }
                }
            }
            complaintStatsErrorLiveData.observe(viewLifecycleOwner) {
                val errorString = StringBuilder()
                errorString.append(resources.getString(R.string.anErrorOccurred))
                    .append(" ${it.message}")
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
                //Might need to put findNavController().navigateUp() -> But as it goes back, it re-calls API for All complaints
            }
            acknowledgementLiveData.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.successfullySent),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess("", true)
            }
            postAcknowledgeErrorLiveData.observe(viewLifecycleOwner) {
                val errorString = StringBuilder()
                errorString.append(resources.getString(R.string.anErrorOccurred))
                    .append(" ${it.message}")
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
            }
            finishLiveData.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.successfullySent),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess("", true)
            }
            postFinishErrorLiveData.observe(viewLifecycleOwner) {
                val errorString = StringBuilder()
                errorString.append(resources.getString(R.string.anErrorOccurred))
                    .append(" ${it.message}")
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
            }
            withdrawLiveData.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.successfullySent),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccess("", true)
            }
            postWithdrawErrorLiveData.observe(viewLifecycleOwner) {
                val errorString = StringBuilder()
                errorString.append(resources.getString(R.string.anErrorOccurred))
                    .append(" ${it.message}")
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
            }
            ratingLiveData.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.successfullySent),
                    Toast.LENGTH_SHORT
                ).show()
            }
            postRatingErrorLiveData.observe(viewLifecycleOwner) {
                val errorString = StringBuilder()
                errorString.append(resources.getString(R.string.anErrorOccurred))
                    .append(" ${it.message}")
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
                binding.ratingBar.rating = 0F
            }
        }
    }

    private fun showCustomDialog() {
        val dialog = AdminDialogFragment()
        dialog.setCustomDialogListener(this)
        dialog.show(requireActivity().supportFragmentManager, "DialogFragment")
    }

    override fun onDataEntered(data: String) {
        onSuccess(data.trim(), false)
    }

    private fun onSuccess(dialogInput: String, isCalledAfterAPISuccess: Boolean) {

        val requestAcknowledge = resources.getString(R.string.acknowledge)
        val requestFinish = resources.getString(R.string.finish)
        val requestWithdraw = resources.getString(R.string.withdraw)

        when (binding.buttonCTA.text) {
            requestAcknowledge -> {
                if (isCalledAfterAPISuccess) {
                    binding.buttonCTA.apply {
                        text = requestFinish
                        backgroundTintList =
                            AppCompatResources.getColorStateList(context, R.color.stroke_green)
                    }
                } else {
                    complaintId?.let {
                        complaintStatusViewModel.postAcknowledgementRequest(it, dialogInput)
                    }
                }
            }
            requestFinish -> {
                if (isCalledAfterAPISuccess) {
                    binding.buttonCTA.visibility = View.GONE
                } else {
                    complaintId?.let {
                        complaintStatusViewModel.postFinishRequest(it, dialogInput)
                    }
                }
            }
            requestWithdraw -> {
                if (isCalledAfterAPISuccess) {
                    binding.apply {
                        buttonCTA.visibility = View.GONE
                        rateThisServiceLayout.visibility = View.VISIBLE
                    }
                } else {
                    complaintId?.let {
                        complaintStatusViewModel.postWithdrawRequest(it, dialogInput)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.COMPLAINTS_STATUS_FRAGMENT
}