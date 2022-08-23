package com.example.baselibrary.utils

interface IMediaPlayer {
    fun setDataSource(path: String)
    fun start()
    fun prepare()
    fun prepareAsync()
    fun pause()
    fun stop()
    fun release()
    fun getCurrentPosition() : Int
    fun isPlaying():Boolean
    fun reset()
}