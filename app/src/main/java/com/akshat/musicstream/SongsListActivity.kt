package com.akshat.musicstream

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import com.akshat.musicstream.adapter.CategoryAdapter
import com.akshat.musicstream.adapter.SongListAdapter
import com.akshat.musicstream.databinding.ActivityMainBinding
import com.akshat.musicstream.databinding.ActivitySongsListBinding
import com.akshat.musicstream.models.CategoryModel
import com.akshat.musicstream.models.SongModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale.Category

class SongsListActivity : AppCompatActivity() {

    companion object{
        lateinit var category:CategoryModel
    }

    lateinit var binding: ActivitySongsListBinding
    lateinit var songsListAdapter : SongListAdapter
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nameTextView.text = category.name
        Glide.with(binding.coverImageView).load(category.coverUrl)
            .apply(
                RequestOptions().transform(RoundedCorners(32))
            )
            .into(binding.coverImageView)

        setupSongsListRecyclerView()


        val totalSongs = intent.getIntExtra("totalSongs",0)
        if (totalSongs == 1){
            binding.totalSongs.text = "$totalSongs song"
        }
        else{
            binding.totalSongs.text = "$totalSongs songs"
        }


        //back button on click
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    fun setupSongsListRecyclerView(){
        songsListAdapter = SongListAdapter(category.songs)
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter= songsListAdapter
    }
}