package com.akshat.musicstream

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.GestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.akshat.musicstream.databinding.ActivityMainBinding
import com.akshat.musicstream.databinding.ActivityPlayerBinding
import com.akshat.musicstream.models.SongModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch




class PlayerActivity : AppCompatActivity() {

    lateinit var gestureDetector: GestureDetector
    private val mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    lateinit var binding1: ActivityMainBinding
    lateinit var binding: ActivityPlayerBinding
    lateinit var exoPlayer: ExoPlayer
    private val isFetchingEnabled = true
    private var isNetworkAvailable = false
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val songId = MyExoplayer.getCurrentSong()?.id
    val coverUrl = MyExoplayer.getCurrentSong()?.coverUrl

    //    val id = MyExoplayer.getCurrentSong()?.id
    val title = MyExoplayer.getCurrentSong()?.title
    val subtitle = MyExoplayer.getCurrentSong()?.subtitle
    val lyrics = MyExoplayer.getCurrentSong()?.lyrics
    val url = MyExoplayer.getCurrentSong()?.url
    val likeRef = FirebaseFirestore.getInstance().collection("likes").document("$userId")

    val songDocumentName = songId.toString() + "-" + userId.toString()
    var playWhenReady = true
    var mediaItemIndex = 0
    var playbackPosition = 0L

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        binding1 = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleTextView.text = title
            binding.songSubtitleTextView.text = subtitle
            binding.lyricsText.text = lyrics

            Glide.with(binding.songCover).load(coverUrl)
                .circleCrop()
                .into(binding.songCover)

            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer


            //back button on click
            binding.backBtn.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }


            binding.repeatBtn.setOnClickListener {
                if (exoPlayer.repeatMode == ExoPlayer.REPEAT_MODE_OFF) {
                    exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    binding.repeatBtn.setImageResource(R.drawable.repeat_on)
                } else {
                    exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                    binding.repeatBtn.setImageResource(R.drawable.repeat)

                }

            }
            binding.likeBtn.setOnClickListener {

                likeSong()
            }
            binding.likedBtn.setOnClickListener {

                unlikeSong()
            }
            binding.downloadBtn.setOnClickListener {
                Toast.makeText(this@PlayerActivity, "Downloading song...", Toast.LENGTH_SHORT)
                    .show()
                binding.downloadBtn.setColorFilter(R.color.white)
            }




            if (lyrics != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.lyricsText.text = Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    binding.lyricsText.text = Html.fromHtml(lyrics)
                }
            } else {
                // ... handle case where no lyrics were found
                Toast.makeText(this@PlayerActivity, "No lyrics found", Toast.LENGTH_SHORT).show()
            }

            likeRef.get().addOnSuccessListener {
                val arrayField = it.get("songs") as? List<String>
                if(arrayField != null){
                    if (arrayField.contains(songId)){
                        binding.likeBtn.visibility = View.GONE
                        binding.likedBtn.visibility = View.VISIBLE

                    }

                }


            }

            lifecycleScope.launch {
                while (isActive) {
                    if (MyExoplayer.getInstance()?.repeatMode == ExoPlayer.REPEAT_MODE_ONE) {
                        binding.repeatBtn.setImageResource(R.drawable.repeat_on)
                    } else
                        binding.repeatBtn.setImageResource(R.drawable.repeat)

                    delay(100)
                }
            }

        }
    }

    private fun releasePlayer() {
        exoPlayer.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }

    }
//    private fun initializePlayer() {
//
//        // Instead of exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
//        exoPlayer.playWhenReady = playWhenReady
//        exoPlayer.prepare()
//    }

    fun likeSong() {

        val data = hashMapOf<String, Any>(
            "songs" to FieldValue.arrayUnion(songId)
        )
//        (data["songs"] as? ArrayList<String>)?.add(songId.toString())


        likeRef.update(data).addOnSuccessListener {
            binding.likeBtn.visibility = View.GONE
            binding.likedBtn.visibility = View.VISIBLE
            Toast.makeText(this, "Song Liked", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            //code for on failure
        }
    }


    fun unlikeSong() {
        val updates = hashMapOf<String, Any>(
            "songs" to FieldValue.arrayRemove(songId)
        )
        likeRef.update(updates).addOnSuccessListener {
            binding.likeBtn.visibility = View.VISIBLE
            binding.likedBtn.visibility = View.GONE
            Toast.makeText(this, "Song Unliked", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

        }


    }

}


