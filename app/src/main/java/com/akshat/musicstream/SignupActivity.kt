package com.akshat.musicstream

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akshat.musicstream.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    lateinit var binding :ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountBtn.setOnClickListener{
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if(!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(),email)){
                binding.emailEditText.setError("Invalid email")
                return@setOnClickListener
            }

            if(password.length < 6){
                binding.passwordEditText.setError("Min length is 7")
                return@setOnClickListener
            }

            if (confirmPassword != password){
                binding.confirmPasswordEditText.setError("Password does not match")
            }


            createAccountWithFirebase(email, password)

        }

        binding.gotoLoginBtn.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }


    fun createAccountWithFirebase(email: String, password : String){

        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setInProgress(false)
                Toast.makeText(applicationContext,"User account created successfully", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener{
                setInProgress(false)
                Toast.makeText(applicationContext,"Failed to create an account", Toast.LENGTH_SHORT).show()
            }

    }


    fun setInProgress(inProgress : Boolean){
        if (inProgress){
            binding.createAccountBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

        }else{
            binding.createAccountBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}








































