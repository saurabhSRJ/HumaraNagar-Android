package com.humara.nagar.ui.common

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.humara.nagar.KohiiProvider
import com.humara.nagar.Logger
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.databinding.FragmentVideoPlayerBinding
import kohii.v1.core.MemoryMode
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii

/** An activity that plays video media. */
class VideoPlayerFragment : BaseFragment(), Playback.Callback {
    private lateinit var binding: FragmentVideoPlayerBinding
    private val args: VideoPlayerFragmentArgs by navArgs()
    private val navController: NavController by lazy {
        findNavController()
    }
    private lateinit var kohii: Kohii

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val videoUri: Uri = args.videoUri
        kohii = KohiiProvider.get(requireContext())
        kohii.register(this, memoryMode = MemoryMode.HIGH)
            .addBucket(binding.clRoot)
        kohii.setUp(videoUri) {
            tag = videoUri
            preload = true
            repeatMode = Player.REPEAT_MODE_ONE
            controller = object : Playback.Controller {
                override fun kohiiCanPause() = true
                override fun kohiiCanStart() = true

                override fun setupRenderer(playback: Playback, renderer: Any?) {
                    if (renderer is PlayerView) {
                        Logger.debugLog("setup")
                        renderer.apply {
                            useController = true
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                        }
                    }
                }

                override fun teardownRenderer(playback: Playback, renderer: Any?) {
                    if (renderer is PlayerView) {
                        Logger.debugLog("tear down")
                        renderer.useController = false
                    }
                }
            }
        }
            .bind(binding.playerView)
        initView()
    }

    private fun initView() {
        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.VIDEO_PLAYER_FRAGMENT
}