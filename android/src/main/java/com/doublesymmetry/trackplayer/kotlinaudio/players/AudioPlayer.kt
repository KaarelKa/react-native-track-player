package com.doublesymmetry.trackplayer.kotlinaudio.players

import android.content.Context
import com.doublesymmetry.trackplayer.kotlinaudio.models.BufferConfig
import com.doublesymmetry.trackplayer.kotlinaudio.models.CacheConfig
import com.doublesymmetry.trackplayer.kotlinaudio.models.PlayerConfig

class AudioPlayer(
    context: Context,
    playerConfig: PlayerConfig = PlayerConfig(),
    bufferConfig: BufferConfig? = null,
    cacheConfig: CacheConfig? = null
) : BaseAudioPlayer(context, playerConfig, bufferConfig, cacheConfig)
