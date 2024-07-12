package com.akshat.musicstream

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.akshat.musicstream.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

object MyExoplayer {
    private var exoPlayer:ExoPlayer? = null

    private var currentSong : SongModel?=null
    private var lyrics : SongModel?=null

    fun getCurrentSong() : SongModel?{
        return currentSong
    }
    fun getLyrics(songId:String) : SongModel?{
        return lyrics
    }

    fun getInstance():ExoPlayer?{
        return exoPlayer
    }
    fun startPlaying(context: Context, song: SongModel?){

        if (exoPlayer==null)
            exoPlayer = ExoPlayer.Builder(context).build()

        if (exoPlayer!!.playbackState == ExoPlayer.STATE_ENDED){
            exoPlayer?.seekTo(0)
            exoPlayer = ExoPlayer.Builder(context).build()
        }

        if(currentSong != song){
            //its a new song so start playing it
            currentSong = song
            updateCount()
            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()
        }


        }
    }

    fun updateCount(){
        currentSong?.id?.let {id->
            FirebaseFirestore.getInstance().collection("songs")
                .document(id)
                .get().addOnSuccessListener {
                    var latestCount = it.getLong("count")
                    if (latestCount == null){
                        latestCount = 1
                    }else{
                        latestCount ++
                    }
                    FirebaseFirestore.getInstance().collection("songs")
                        .document(id)
                        .update(mapOf("count" to latestCount))
                }
        }
    }
}