package com.humara.nagar

import android.content.Context
import kohii.v1.exoplayer.ExoPlayerConfig
import kohii.v1.exoplayer.Kohii
import kohii.v1.exoplayer.createKohii
import kohii.v1.utils.Capsule

object KohiiProvider {
    private val capsule = Capsule<Kohii, Context>(creator = { context ->
        createKohii(context, ExoPlayerConfig.FAST_START)
    })

    fun get(context: Context): Kohii = capsule.get(context)
}