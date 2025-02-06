package com.issue.smoothtransition

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

@OptIn(UnstableApi::class)
suspend fun silenceVideo(context: Context, uri: Uri, duration: Duration): Uri {
    val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(uri))
        .setRemoveAudio(true).build()

    val composition = Composition.Builder(EditedMediaItemSequence(editedMediaItem)).build()
    val transformer = Transformer.Builder(context).build()
    return transformer.run(context, composition).toUri()
}

@OptIn(UnstableApi::class)
suspend fun convertImageToVideo(context: Context, uri: Uri, duration: Duration): Uri {
    val editedMediaItem = EditedMediaItem.Builder(
        MediaItem.fromUri(uri).buildUpon().setImageDurationMs(duration.inWholeMilliseconds)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder().setStartPositionMs(0)
                    .setEndPositionMs(duration.inWholeMilliseconds).build()
            ).build()
    )
        .setFrameRate(24)
        .build()

    val composition =
        Composition.Builder(EditedMediaItemSequence(editedMediaItem))
            .build()

    val transformer = Transformer.Builder(context).build()
    return transformer.run(context, composition).toUri()
}

@OptIn(UnstableApi::class)
suspend fun Transformer.run(context: Context, composition: Composition): String =
    withContext(Dispatchers.Main) {
        val file = context.filesDir.resolve("transition${System.currentTimeMillis()}.mp4")

        suspendCancellableCoroutine<Unit> {
            val listener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    it.resume(Unit)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    it.resumeWithException(exportException)
                }
            }

            addListener(listener)
            start(composition, file.path)

            it.invokeOnCancellation {
                Handler(Looper.getMainLooper()).post {
                    removeListener(listener)
                    cancel()
                }
            }
        }

        file.toUri().toString()
    }
