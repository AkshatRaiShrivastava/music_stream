package com.akshat.musicstream

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.akshat.musicstream.SongsListActivity.Companion.category
import com.akshat.musicstream.adapter.SongListAdapter
import com.akshat.musicstream.databinding.ActivityLikedSongsBinding
import com.akshat.musicstream.databinding.ActivitySongsListBinding

class LikedSongsActivity : AppCompatActivity() {
    lateinit var binding : ActivityLikedSongsBinding
    lateinit var songsListAdapter : SongListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikedSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)





    }


}