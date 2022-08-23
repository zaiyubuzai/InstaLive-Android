package com.example.baselibrary.utils

import android.media.MediaPlayer

object MediaPlayerManager : IMediaPlayer {

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var currentPath:String ?= null

    override fun setDataSource(path: String) {
        currentPath = path
        if (path.isEmpty()) {
            return
        }
        try {
            mediaPlayer.setDataSource(path)
        } catch (e: Exception) {
        }

    }

    override fun start() {
        if (currentPath?.isEmpty() == true) {
            return
        }
        mediaPlayer.start()
    }

     fun seekTo(position:Int) {
        mediaPlayer.seekTo(position)
    }
    override fun prepare() {
        if (currentPath?.isEmpty() == true || currentPath == null) {
            return
        }
        mediaPlayer.prepare()
    }

    override fun prepareAsync() {
        if (currentPath?.isEmpty() == true || currentPath == null) {
            return
        }
        mediaPlayer.prepareAsync()
    }

    override fun pause() {
        if (currentPath?.isEmpty() == true) {
            return
        }
        mediaPlayer.pause()
    }

    override fun stop() {
        if (currentPath?.isEmpty() == true) {
            return
        }
        mediaPlayer.stop()
    }

    override fun release() {
        if (currentPath?.isEmpty() == true) {
            return
        }
        mediaPlayer.release()
    }

    override fun getCurrentPosition():Int {
        if (currentPath?.isEmpty() == true) {
            return 0
        }
        return mediaPlayer.currentPosition
    }

    override fun isPlaying(): Boolean {
        if (currentPath?.isEmpty() == true) {
            return false
        }
        return mediaPlayer.isPlaying
    }

    override fun reset() {
        mediaPlayer.reset()
    }

    fun setPlayerListener(listener: MediaPlayer.OnPreparedListener){
       mediaPlayer.setOnPreparedListener{
           listener.onPrepared(it)
       }
   }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener){
        mediaPlayer.setOnCompletionListener {
            listener.onCompletion(it)
        }
    }

    fun setOnErrorListener(listener: MediaPlayer.OnErrorListener){
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            listener.onError(mp,what,extra)
        }
    }

    fun getCurrentPath(): String? {
        return currentPath
    }

}