package com.humara.nagar.ui.home.create_post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentCreatePollBinding

class CreatePollFragment : BaseFragment() {
    private lateinit var binding: FragmentCreatePollBinding
    private val navController: NavController by lazy {
        findNavController()
    }
    private val createPollViewModel: CreatePollViewModel by viewModels {
        ViewModelFactory()
    }
    private val pollDurations = arrayListOf(7, 3, 1)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreatePollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        createPollViewModel.run {
            doneButtonStateLiveData.observe(viewLifecycleOwner) { enabled ->
                binding.btnDone.isEnabled = enabled
            }
            thirdOptionLiveData.observe(viewLifecycleOwner) {
                binding.inputThirdOption.visibility = View.VISIBLE
            }
            fourthOptionLiveData.observe(viewLifecycleOwner) {
                binding.inputFourthOption.visibility = View.VISIBLE
                binding.btnAddOption.visibility = View.GONE
            }
            pollRequestLiveData.observe(viewLifecycleOwner) {
                setFragmentResult(CreatePostFragment.POLL_RESULT_REQUEST, bundleOf(CreatePostFragment.POLL_DATA to it))
                navController.navigateUp()
            }
        }
    }

    private fun initView() {
        binding.run {
            btnCross.setOnClickListener {
                navController.navigateUp()
            }
            inputQuestion.apply {
                switchToMultiLined(3)
                setUserInputListener {
                    createPollViewModel.setQuestion(it)
                }
            }
            inputFirstOption.apply {
                setUserInputListener {
                    createPollViewModel.setFirstOption(it)
                }
            }
            inputSecondOption.apply {
                setUserInputListener {
                    createPollViewModel.setSecondOption(it)
                }
            }
            inputThirdOption.apply {
                setUserInputListener {
                    createPollViewModel.setThirdOption(it)
                }
            }
            inputFourthOption.apply {
                setUserInputListener {
                    createPollViewModel.setFourthOption(it)
                }
            }
            btnAddOption.setOnClickListener {
                if (inputThirdOption.isVisible) {
                    inputFourthOption.visibility = View.VISIBLE
                    it.visibility = View.GONE
                } else {
                    inputThirdOption.visibility = View.VISIBLE
                }
            }
            val pollDurationOptions = arrayListOf<String>()
            for (item in pollDurations) {
                pollDurationOptions.add(resources.getQuantityString(R.plurals.n_days, item, item))
            }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, pollDurationOptions)
            pollDurationSpinner.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    createPollViewModel.setPollDuration(pollDurations[position])
                }
                setText(adapter.getItem(1).toString(), false);
            }
            btnDone.setOnClickListener {
                createPollViewModel.getPollRequestObjectWithCollectedData()
            }
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.CREATE_POLL_FRAGMENT
}