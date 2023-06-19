package com.doublesymmetry.trackplayer.kotlinaudio.models

data class PlayWhenReadyChangeData(
    val playWhenReady: Boolean,
    val pausedBecauseReachedEnd: Boolean
)
