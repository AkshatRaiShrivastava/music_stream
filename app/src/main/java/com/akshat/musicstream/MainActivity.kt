package com.akshat.musicstream

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.text.TextAnnotation
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.PlaybackStats.PLAYBACK_STATE_ENDED
import androidx.media3.exoplayer.analytics.PlaybackStats.PLAYBACK_STATE_STOPPED
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akshat.musicstream.adapter.CategoryAdapter
import com.akshat.musicstream.adapter.SectionSongListAdapter
import com.akshat.musicstream.databinding.ActivityMainBinding
import com.akshat.musicstream.models.CategoryModel
import com.akshat.musicstream.models.SongModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    lateinit var exoPlayer: ExoPlayer
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val likeRef = db.collection("likes").document("$userId")
    val songsDb = db.collection("songs")

    val allSongs = listOf(songsDb)

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        likeRef.get().addOnSuccessListener {
            if (!it.exists()) {
                likeRef.set(mapOf("songs" to listOf<String>()))
            }
        }


        getCategories()
        setupLikedSection(
            "liked_songs",
            binding.likedSongsLayout,
            binding.likedSongsTitle,
            binding.likedSongsRecyclerView
        )
        setupSections(
            "section1",
            binding.section1MainLayout,
            binding.section1Title,
            binding.section1RecyclerView
        )
        setupSections(
            "section2",
            binding.section2MainLayout,
            binding.section2Title,
            binding.section2RecyclerView
        )
//        setupLikedSongs(
//            "liked_songs",
//            binding.likedSongsLayout,
//            binding.likedSongsTitle,
//            binding.likedSongsRecyclerView
//        )

//        setupMostlyPlayed(
//            "mostly_played",
//            binding.mostlyPlayedMainLayout,
//            binding.mostlyPlayedTitle,
//            binding.mostlyPlayedRecyclerView
//        )
        setupSections(
            "section3",
            binding.section3MainLayout,
            binding.section3Title,
            binding.section3RecyclerView
        )

        binding.optionBtn.setOnClickListener {
            showPopupMenu()
        }

        binding.playPauseImage.setOnClickListener {
            if (MyExoplayer.getInstance()?.isPlaying == true) {
                MyExoplayer.getInstance()?.pause()
                binding.playPauseImage.setImageResource(R.drawable.play_icon)
            } else {

                MyExoplayer.getInstance()?.play()
                binding.playPauseImage.setImageResource(R.drawable.pause_icon)

            }
        }

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val welcomeText = when (currentHour) {
            in 0..11 -> "Good morning!"
            in 12..17 -> "Good afternoon!"
            in 18..23 -> "Good evening!"
            else -> "Welcome!"
        }
        binding.welcomeTxt.text = welcomeText
        binding.usernameText.text = FirebaseAuth.getInstance().currentUser?.displayName

        binding.repeatBtn.setOnClickListener {
            if (MyExoplayer.getInstance()!!.repeatMode == Player.REPEAT_MODE_OFF) {
                MyExoplayer.getInstance()?.repeatMode = Player.REPEAT_MODE_ONE
                binding.repeatBtn.setImageResource(R.drawable.repeat_on)
            } else {
                MyExoplayer.getInstance()?.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(R.drawable.repeat)
            }
        }


        binding.searchBarEditText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                TODO("Not yet implemented")
            }

            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s.toString().trim()
                filterSongs(searchQuery)
            }

        }
        )
        MyExoplayer.getInstance()?.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {

            }

            override fun onPlaybackStateChanged(playbackState: Int) {
            }
        })

        binding.playerViewSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }
        })





        lifecycleScope.launch {
            while (isActive) {

                if (MyExoplayer.getInstance()?.repeatMode == Player.REPEAT_MODE_ONE) {
                    binding.repeatBtn.setImageResource(R.drawable.repeat_on)
                } else
                    binding.repeatBtn.setImageResource(R.drawable.repeat)


                if (MyExoplayer.getInstance()?.isPlaying == true) {
                    binding.playPauseImage.setImageResource(R.drawable.pause_icon)
                } else
                    binding.playPauseImage.setImageResource(R.drawable.play_icon)





                delay(1000)
            }
        }

    }

    fun filterSongs(query: String) {
        songsDb.startAt(query).get().addOnSuccessListener {

        }


        // Update your UI with the filteredSongs list (e.g., update a RecyclerView)
    }

    fun showPopupMenu() {
        val popupMenu = PopupMenu(this, binding.optionBtn)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }

                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
            }
            false
        }
    }


    fun logout() {
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }


    override fun onResume() {
        super.onResume()
        showPlayerView()
        if (MyExoplayer.getInstance()?.isPlaying == true) {
            binding.playPauseImage.setImageResource(R.drawable.pause_icon)
        } else {
            binding.playPauseImage.setImageResource(R.drawable.play_icon)
        }
        setupLikedSection(
            "liked_songs",
            binding.likedSongsLayout,
            binding.likedSongsTitle,
            binding.likedSongsRecyclerView
        )
        binding.usernameText.text = FirebaseAuth.getInstance().currentUser?.displayName

    }


    fun showPlayerView() {
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.songCoverImageView)
        } ?: run {
            binding.playerView.visibility = View.GONE
        }
    }


    //Categories
    fun getCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>) {
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    //Sections

    fun setupSections(
        id: String,
        mainLayout: RelativeLayout,
        titleView: TextView,
        recyclerView: RecyclerView
    ) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                    }
                }
            }
    }

    fun setupLikedSection(
        id: String,
        mainLayout: RelativeLayout,
        titleView: TextView,
        recyclerView: RecyclerView
    ) {
        likeRef.get().addOnSuccessListener {
            val arrayField: Any? = it?.get("songs")

            FirebaseFirestore.getInstance().collection("sections")
                .document(id).get().addOnSuccessListener { itt ->
                    val section = itt.toObject(CategoryModel::class.java)
                    if (section != null) {
                        section.songs = arrayField as List<String>
                    }
                    section?.apply {
                        mainLayout.visibility = View.VISIBLE
                        titleView.text = name
                        recyclerView.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false

                        )
                        //Toast.makeText(this@MainActivity, arrayField.size.toString(),Toast.LENGTH_SHORT).show()
                        recyclerView.adapter = SectionSongListAdapter(songs)
                        mainLayout.setOnClickListener {
                            SongsListActivity.category = section
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    SongsListActivity::class.java
                                )
                            )
                        }
                    }
                }
        }
    }
}