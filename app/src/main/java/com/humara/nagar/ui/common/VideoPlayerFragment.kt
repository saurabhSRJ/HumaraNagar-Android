package com.humara.nagar.ui.common

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ui.PlayerView
import com.humara.nagar.KohiiProvider
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.databinding.FragmentVideoPlayerBinding
import kohii.v1.core.Common
import kohii.v1.core.MemoryMode
import kohii.v1.core.Playback

/** An activity that plays video media. */
class VideoPlayerFragment : BaseFragment() {
    private lateinit var binding: FragmentVideoPlayerBinding
    private val args: VideoPlayerFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val videoUri: Uri = args.videoUri
        val kohii = KohiiProvider.get(requireContext())
        kohii.register(this, memoryMode = MemoryMode.HIGH)
            .addBucket(binding.clRoot)
        kohii.setUp(videoUri) {
            tag = videoUri
            preload = true
            repeatMode = Common.REPEAT_MODE_ONE
            controller = object : Playback.Controller {
                override fun kohiiCanPause() = true
                override fun kohiiCanStart() = true

                override fun setupRenderer(playback: Playback, renderer: Any?) {
                    if (renderer is PlayerView) {
                        renderer.useController = true
                        renderer.setControlDispatcher(kohii.createControlDispatcher(playback))
                    }
                }

                override fun teardownRenderer(playback: Playback, renderer: Any?) {
                    if (renderer is PlayerView) {
                        renderer.useController = false
                        renderer.setControlDispatcher(null)
                    }
                }
            }
        }
            .bind(binding.playerView)
    }

    override fun getScreenName() = AnalyticsData.ScreenName.VIDEO_PLAYER_FRAGMENT
}