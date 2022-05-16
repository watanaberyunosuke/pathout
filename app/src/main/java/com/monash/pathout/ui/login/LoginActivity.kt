package com.monash.pathout.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.monash.pathout.R
import com.monash.pathout.databinding.ActivityLoginBinding
import com.monash.pathout.ui.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        FirebaseApp.initializeApp(this)

        if (Firebase.auth.currentUser != null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.signupButton)
        val loginButton = findViewById<Button>(R.id.signinButton)

        registerButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

        loginButton.setOnClickListener {
            val emailText = emailEditText.text.toString()
            val passwordText = passwordEditText.text.toString()
            signIn(emailText, passwordText)
        }


    }

    private fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_LONG).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmailPassword:success")
                currentUser = Firebase.auth.currentUser!!
                currentUser.reload()
                startActivity(Intent(this@LoginActivity, WelcomeActivity::class.java))
            } else {
                Log.w(TAG, "signInWithEmail:Failed", task.exception)
                toastMsg("Sign in with email failed")
            }
        }
    }


    private fun toastMsg(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "EmailPasswordLogin"
    }
}