package com.issue.smoothtransition

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import com.issue.smoothtransition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.playWhenReady = true
        val concatenatingMediaSource = ConcatenatingMediaSource2.Builder()
            .useDefaultMediaSourceFactory(this)
            .add(createClippedMediaItem("1.mp4"))
            .add(createClippedMediaItem("2.mov"))
            .build()
        exoPlayer.setMediaSource(concatenatingMediaSource)
        exoPlayer.prepare()

        val playerView = binding.playerView
        playerView.player = exoPlayer

        setContentView(binding.root)
    }

    private fun createClippedMediaItem(assetPath: String): MediaItem {
        return MediaItem.fromUri("asset:///$assetPath")
            .buildUpon()
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(3000)
                    .setEndPositionMs(6000)
                    .build()
            )
            .build()
    }
}