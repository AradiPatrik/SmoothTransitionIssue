package com.issue.smoothtransition

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import com.issue.smoothtransition.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val exoPlayer =
            ExoPlayer.Builder(this).setRenderersFactory(DefaultRenderersFactory(this).apply {
                experimentalSetEnableMediaCodecVideoRendererPrewarming(true)
            }).build()


        // TODO: uncomment section to test different scenarios, check logs to see current playback delay
        // when player is transitioning from video to image there's a 200 ms jank on my device
//        setupWithNormalImagesAndMeasure(exoPlayer)

        // transitions between videos are smooth
        // setupWithOnlyVideosAndMeasure(exoPlayer)

        // same as first scenario: there's a 200ms jank on my device
        // convertImagesToVideosThenSetupExoplayerAndMeasure(exoPlayer)

        // when removing audio track from videos the issue seems to be fixed on my deviece. Interestingly enough it will also remove the initial 200ms jank
        setupExoPlayerWithSilencedVideosAndImagesAndMeasure(exoPlayer)
        setContentView(binding.root)
    }

    @OptIn(UnstableApi::class)
    private fun setupWithNormalImagesAndMeasure(exoPlayer: ExoPlayer) {
        var startTime = 0L
        exoPlayer.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                if (startTime == 0L) {
                    startTime = System.currentTimeMillis()
                }
            }
        })

        exoPlayer.playWhenReady = true
        val concatenatingMediaSource = createMixedImageVideoMediaSource(this)
        exoPlayer.setMediaSource(concatenatingMediaSource)
        exoPlayer.prepare()

        val playerView = binding.playerView
        playerView.player = exoPlayer
        lifecycleScope.launch {
            while (true) {
                delay(300)
                Log.d(
                    "measure",
                    abs(System.currentTimeMillis() - startTime - exoPlayer.currentPosition).toString()
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupWithOnlyVideosAndMeasure(exoPlayer: ExoPlayer) {
        var startTime = 0L
        exoPlayer.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                if (startTime == 0L) {
                    startTime = System.currentTimeMillis()
                }
            }
        })

        exoPlayer.playWhenReady = true
        val concatenatingMediaSource = createVideoOnlyMediaSource(this)
        exoPlayer.setMediaSource(concatenatingMediaSource)
        exoPlayer.prepare()

        val playerView = binding.playerView
        playerView.player = exoPlayer
        lifecycleScope.launch {
            while (true) {
                delay(300)
                Log.d(
                    "measure",
                    abs(System.currentTimeMillis() - startTime - exoPlayer.currentPosition).toString()
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun convertImagesToVideosThenSetupExoplayerAndMeasure(exoPlayer: ExoPlayer) {
        lifecycleScope.launch {
            val (car, party) = withContext(Dispatchers.IO) {
                listOf(
                    convertImageToVideo(this@MainActivity, "asset:///car.jpg".toUri(), 3.seconds),
                    convertImageToVideo(this@MainActivity, "asset:///party.jpg".toUri(), 3.seconds)
                )
            }

            val mediaSource = ConcatenatingMediaSource2.Builder()
                .useDefaultMediaSourceFactory(this@MainActivity)
                .add(createClippedMediaItemFromAsset("food1.mp4"))
                .add(createClippedMediaItemFromFile(car))
                .add(createClippedMediaItemFromAsset("hotpot.mp4"))
                .add(createClippedMediaItemFromFile(party))
                .add(createClippedMediaItemFromAsset("food1.mp4"))
                .build()

            var startTime = 0L
            exoPlayer.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    super.onRenderedFirstFrame()
                    if (startTime == 0L) {
                        startTime = System.currentTimeMillis()
                    }
                }
            })

            exoPlayer.playWhenReady = true
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            val playerView = binding.playerView
            playerView.player = exoPlayer
            lifecycleScope.launch {
                while (true) {
                    delay(300)
                    Log.d(
                        "measure",
                        abs(System.currentTimeMillis() - startTime - exoPlayer.currentPosition).toString()
                    )
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun setupExoPlayerWithSilencedVideosAndImagesAndMeasure(exoPlayer: ExoPlayer) =
        lifecycleScope.launch {
            var startTime = 0L
            exoPlayer.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    super.onRenderedFirstFrame()
                    if (startTime == 0L) {
                        startTime = System.currentTimeMillis()
                    }
                }
            })

            exoPlayer.playWhenReady = true
            val concatenatingMediaSource =
                withContext(Dispatchers.IO) { createMixedSilencedVideoAndImageMediaSource(this@MainActivity) }
            exoPlayer.setMediaSource(concatenatingMediaSource)
            exoPlayer.prepare()

            val playerView = binding.playerView
            playerView.player = exoPlayer
            lifecycleScope.launch {
                while (true) {
                    delay(300)
                    Log.d(
                        "measure",
                        abs(System.currentTimeMillis() - startTime - exoPlayer.currentPosition).toString()
                    )
                }
            }
        }
}

@OptIn(UnstableApi::class)
fun createMixedImageVideoMediaSource(context: Context): ConcatenatingMediaSource2 {
    return ConcatenatingMediaSource2.Builder()
        .useDefaultMediaSourceFactory(context)
        .add(createClippedMediaItemFromAsset("food1.mp4"))
        .add(
            MediaItem.fromUri("asset:///car.jpg").buildUpon()
                .setImageDurationMs(1000)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(0)
                        .setEndPositionMs(1000)
                        .build()
                )
                .build()
        )
        .add(createClippedMediaItemFromAsset("hotpot.mp4"))
        .add(
            MediaItem.fromUri("asset:///party.jpg").buildUpon()
                .setImageDurationMs(1000)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(0)
                        .setEndPositionMs(1000)
                        .build()
                )
                .build()
        )
        .add(createClippedMediaItemFromAsset("food1.mp4"))
        .build()
}

@OptIn(UnstableApi::class)
fun createVideoOnlyMediaSource(context: Context): ConcatenatingMediaSource2 {
    return ConcatenatingMediaSource2.Builder()
        .useDefaultMediaSourceFactory(context)
        .add(createClippedMediaItemFromAsset("food1.mp4"))
        .add(createClippedMediaItemFromAsset("hotpot.mp4"))
        .add(createClippedMediaItemFromAsset("food1.mp4"))
        .build()
}

@OptIn(UnstableApi::class)
suspend fun createMixedSilencedVideoAndImageMediaSource(context: Context): ConcatenatingMediaSource2 {
    val (food1, hotpot) = listOf(
        silenceVideo(context, "asset:///food1.mp4".toUri(), 3.seconds),
        silenceVideo(context, "asset:///hotpot.mp4".toUri(), 3.seconds)
    )
    return ConcatenatingMediaSource2.Builder()
        .useDefaultMediaSourceFactory(context)
        .add(createClippedMediaItemFromFile(food1))
        .add(
            MediaItem.fromUri("asset:///car.jpg").buildUpon()
                .setImageDurationMs(1000)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(0)
                        .setEndPositionMs(1000)
                        .build()
                )
                .build()
        )
        .add(createClippedMediaItemFromFile(hotpot))
        .add(
            MediaItem.fromUri("asset:///party.jpg").buildUpon()
                .setImageDurationMs(1000)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(0)
                        .setEndPositionMs(1000)
                        .build()
                )
                .build()
        )
        .add(createClippedMediaItemFromFile(food1))
        .build()
}

fun createClippedMediaItemFromFile(fileUri: Uri): MediaItem {
    return MediaItem.fromUri(fileUri)
        .buildUpon()
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setEndPositionMs(3000)
                .build()
        )
        .build()
}

fun createClippedMediaItemFromAsset(assetPath: String): MediaItem {
    return MediaItem.fromUri("asset:///$assetPath")
        .buildUpon()
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setEndPositionMs(3000)
                .build()
        )
        .build()
}