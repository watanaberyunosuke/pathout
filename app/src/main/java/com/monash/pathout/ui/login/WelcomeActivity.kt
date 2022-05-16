package com.monash.pathout.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.monash.pathout.R
import com.monash.pathout.databinding.WelcomePageBinding
import com.monash.pathout.ui.MainActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: WelcomePageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val textView = findViewById<TextView>(R.id.usernamePlace)
        val currentUser: String = Firebase.auth.currentUser?.email ?: ""

        textView.text = currentUser

        Handler().postDelayed({
            val mainIntent = Intent(this@WelcomeActivity, MainActivity::class.java)
            this.startActivity(mainIntent)
            finish()
        }, 1500)
    }

}