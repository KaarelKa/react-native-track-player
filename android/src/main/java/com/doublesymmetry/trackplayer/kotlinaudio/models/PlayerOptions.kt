package com.doublesymmetry.trackplayer.kotlinaudio.models

interface PlayerOptions {
  var alwaysPauseOnInterruption: Boolean
}

internal data class DefaultPlayerOptions(
    override var alwaysPauseOnInterruption: Boolean = false,
) : PlayerOptions
