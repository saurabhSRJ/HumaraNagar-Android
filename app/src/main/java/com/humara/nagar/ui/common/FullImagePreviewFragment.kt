package com.humara.nagar.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.humara.nagar.adapter.FullImagePreviewAdapter
import com.humara.nagar.adapter.ThumbnailAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.databinding.FragmentFullImagePreviewBinding


class FullImagePreviewFragment : BaseFragment() {

    private var _binding: FragmentFullImagePreviewBinding? = null
    private val binding get() = _binding!!
    private val args: FullImagePreviewFragmentArgs by navArgs()
    private lateinit var fullImagePreviewAdapter: FullImagePreviewAdapter
    private lateinit var thumbnailAdapter: ThumbnailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFullImagePreviewBinding.inflate(layoutInflater, container, false)

        binding.close.setOnClickListener {
            findNavController().navigateUp()
        }

        initView()

        return binding.root
    }

    private fun initView() {

        binding.apply {
            fullImagePreviewAdapter = FullImagePreviewAdapter(requireContext())
            fullImagePreviewVP.adapter = fullImagePreviewAdapter
            thumbnailRCV.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            thumbnailAdapter = ThumbnailAdapter(requireContext()) { urlString ->
                fullImagePreviewVP.currentItem = args.imagesList.indexOf(urlString)
            }
            thumbnailRCV.adapter = thumbnailAdapter
            fullImagePreviewVP.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // update thumbnail border color
                    thumbnailAdapter.updateSelectedPosition(position)
                    // smooth scroll to corresponding position when thumbnail is outside the view
                    binding.thumbnailRCV.smoothScrollToPosition(position)
                }
            })
        }

        fullImagePreviewAdapter.addImages(args.imagesList.toList())
        thumbnailAdapter.addImages(args.imagesList.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.IMAGE_PREVIEW_FRAGMENT
}