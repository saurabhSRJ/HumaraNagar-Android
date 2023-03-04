package com.humara.nagar.ui.residents

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.humara.nagar.R
import com.humara.nagar.adapter.AllResidentsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentResidentsBinding
import com.humara.nagar.ui.residents.model.FiltersResponse
import com.humara.nagar.ui.residents.model.Residents
import com.humara.nagar.utils.Utils


class ResidentsFragment : BaseFragment() {

    private var _binding: FragmentResidentsBinding? = null
    private val residentsViewModel by viewModels<ResidentsViewModel> {
        ViewModelFactory()
    }
    private lateinit var residentAdapter: AllResidentsAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResidentsBinding.inflate(inflater, container, false)

        initObservers()
        initView()

        handleSearchFilter()

        return binding.root
    }

    private fun handleChipFilter(filtersResponse: FiltersResponse) {

        //Create the chips dynamically from the filter response
        for ((count, filter) in filtersResponse.filters.withIndex()) {
            val newChip = Chip(requireContext(), null, R.attr.FilterChips)
            newChip.text = filter
            newChip.id = count
            newChip.elevation = 2F
            binding.chipGroupFilter.addView(newChip)
        }

        var previousFilterId = -1   //Saves the previous checkedID of chips
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedId ->

            if (checkedId.isNotEmpty()) {
                val cid = checkedId[0]
                val chip: Chip? = group.findViewById(cid)
                if (chip != null) {
                    if (previousFilterId != - 1) {
                        //Somethings was checked before this, not the first time
                        group.findViewById<Chip>(previousFilterId).isCloseIconVisible = false
                    }
                    //Check the current item and save the value
                    chip.isCloseIconVisible = true
                    previousFilterId = cid
                } else {
                    //Nothing is checked
                }
            } else {
                //Enabled chip was disabled
                group.findViewById<Chip>(previousFilterId).isCloseIconVisible = false
                previousFilterId = -1
            }
        }
    }

    private fun handleSearchFilter() {
        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val residentList = residentsViewModel.residentsResponse.value?.Residents
                if (p0.toString().isNotEmpty()) {
                    filter(Utils.toStringWithoutSpaces(p0.toString().lowercase()), residentList)
                } else {
                    if (residentList != null) {
                        residentAdapter.setData(residentList)
                    }
                }
            }
        })
    }

    private fun filter(search: String, residentList: ArrayList<Residents>?) {
        val filterList = mutableListOf<Residents>()
        if (search.isNotEmpty()) {
            if (residentList != null) {
                for (current in residentList) {
                    if (Utils.toStringWithoutSpaces(current.name!!.lowercase()).contains(search)) {
                        filterList.add(current)
                    }
                }
            }
            residentAdapter.setData(filterList)
        } else
            residentList?.let { residentAdapter.setData(it) }
    }

    private fun initView() {
        binding.apply {
            residentAdapter = AllResidentsAdapter(requireContext()) {
                //Do something here onClick Card (it -> Residents)
            }
            residentRCV.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                setHasFixedSize(false)
                adapter = residentAdapter
            }
        }
    }

    private fun initObservers() {
        residentsViewModel.run {
            observeProgress(this, false)
            observerException(this)

            fetchAllFilters()
            fetchAllResidents()

            residentsResponse.observe(viewLifecycleOwner) {
                residentAdapter.setData(it.Residents)
            }
            filtersResponse.observe(viewLifecycleOwner) {
                handleChipFilter(it)
            }
            residentErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(resources.getString(R.string.anErrorOccurred), it.message.toString())
            }
            chipFilterErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(resources.getString(R.string.anErrorOccurred), it.message.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.RESIDENTS_FRAGMENT
}