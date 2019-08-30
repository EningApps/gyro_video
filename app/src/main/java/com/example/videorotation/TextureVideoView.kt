package com.example.videorotation

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView

class TextureVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {

    init {
        surfaceTextureListener = this
    }

    private var mediaPlayer: MediaPlayer? = null

    private var isPrepared = false
    private var isPendingPlay = false

    var resId: Int? = null
        set(value) {
            field = value
            createMediaPlayer()
        }

    var path: String? = null
        set(value) {
            field = value
            createMediaPlayer()
        }

    var onVideoCompleteListener: (() -> Unit)? = null
    var videoPreparedListener: (() -> Unit)? = null

    fun play() {
        if (isPrepared) {
            isPendingPlay = false
            mediaPlayer?.start()
        } else {
            isPendingPlay = true
        }
    }

    fun pause() {
        isPendingPlay = false
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        createMediaPlayer()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        mediaPlayer?.release()
        mediaPlayer = null
        return false
    }

    private fun createMediaPlayer() {
        if (mediaPlayer == null && isAvailable) {
            resId?.let { resId ->
                mediaPlayer = MediaPlayer.create(context, resId).also {
                    setMediaPlayerParams(it)
                }
            }
            path?.let { path ->
                mediaPlayer = MediaPlayer().also {
                    try {
                        it.setDataSource(path)
                        it.prepare()
                        setMediaPlayerParams(it)
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun setMediaPlayerParams(mediaPlayer: MediaPlayer) {
        mediaPlayer.setScreenOnWhilePlaying(true)
        mediaPlayer.setSurface(Surface(surfaceTexture))
        mediaPlayer.setOnPreparedListener {
            videoPreparedListener?.invoke()
            isPrepared = true
            mediaPlayer.start()
            if (!isPendingPlay) {
                mediaPlayer.pause()
            }
        }
        mediaPlayer.setOnCompletionListener {
            onVideoCompleteListener?.invoke()
        }
    }
}