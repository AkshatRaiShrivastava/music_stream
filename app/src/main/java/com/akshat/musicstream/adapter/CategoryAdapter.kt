package com.akshat.musicstream.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akshat.musicstream.SongsListActivity
import com.akshat.musicstream.databinding.CategoryItemRecyclerRowBinding
import com.akshat.musicstream.models.CategoryModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class CategoryAdapter (private val categoryList : List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding : CategoryItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindData(category : CategoryModel){
            binding.nameTextView.text = category.name
            Glide.with(binding.coverImageView).load(category.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.coverImageView)
//            Log.i("Songs",category.songs.size.toString())

            //Start SongsList Activity

            val context = binding.root.context
            binding.root.setOnClickListener{
                SongsListActivity.category = category
//                context.startActivity(Intent(context,SongsListActivity::class.java))
                val intent = Intent(context,SongsListActivity::class.java)
                intent.putExtra("totalSongs", category.songs.size)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CategoryItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(categoryList[position])
    }

}