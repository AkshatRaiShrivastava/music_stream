package com.akshat.musicstream

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akshat.musicstream.databinding.ActivitySettingsBinding
import com.akshat.musicstream.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SettingsActivity : AppCompatActivity() {
    lateinit var binding : ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val user = FirebaseAuth.getInstance().currentUser

        user?.displayName?.let {
            if (it.isEmpty()){
                return@let
            }

            binding.nameEditText.setText(it)
        }

        binding.backBtn.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.resetPasswordBtn.setOnClickListener{
            startActivity(Intent(this,ResetPasswordActivity::class.java))
        }

        binding.applyBtn.setOnClickListener{
            val name = binding.nameEditText.text.toString()
            if(name.isEmpty()){
                binding.nameEditText.setError("Name is required")
                return@setOnClickListener
            }


            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) {
                        Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
                    }
                }
        }



    }
}