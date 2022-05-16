package com.monash.pathout.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monash.pathout.R
import com.monash.pathout.model.User
import com.monash.pathout.util.Validator

class SignupActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "FirestoreAddUserInfo"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up)
        val validator = Validator()
        val registerButton = findViewById<Button>(R.id.addButton)
        val userNameText = findViewById<EditText>(R.id.userNameEditText)
        val firstNameText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameText = findViewById<EditText>(R.id.lastNameEditText)
        val phoneNumberText = findViewById<EditText>(R.id.phoneNumberEditText)
        val reenterPasswordEditText = findViewById<EditText>(R.id.reenterPasswordEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val addressEditText = findViewById<EditText>(R.id.addressEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        registerButton.setOnClickListener {
            val username = userNameText.text.toString()
            val firstName = firstNameText.text.toString()
            val lastName = lastNameText.text.toString()
            val email = emailEditText.text.toString()
            val postalAddress = addressEditText.text.toString()
            val phone = phoneNumberText.text.toString()
            val password = passwordEditText.text.toString()
            val reEnterPassword = reenterPasswordEditText.text.toString()
            val passwordMatch = password == reEnterPassword

            // data validation - email and password arent empty
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
            ) {
                val msg = "Empty Email or Password"
                toastMsg(msg)
            } else if (password.length < 6) {
                // error message - password must be longer than 6 characters (password validation)
                val msg = "Password is too short - must be longer than 6 characters"
                toastMsg(msg)
            } else if (!passwordMatch) {
                // error message - both passwords must match (password validation)
                val msg = "Passwords must match."
                toastMsg(msg)
            } else if (!validator.phoneValidator(phone)) {
                val msg = "The phone number you have entered is not correct"
                toastMsg(msg)
            } else if (!validator.emailValidator(email)) {
                val msg = "The email you have entered is not correct"
                toastMsg(msg)
            } else if (TextUtils.isEmpty(firstName)) {
                val msg = "Empty First name"
                toastMsg(msg)
            } else if (TextUtils.isEmpty(lastName)) {
                val msg = "Empty Last Name"
                toastMsg(msg)
            } else if (TextUtils.isEmpty(postalAddress)) {
                val msg = "Empty Postal Address"
                toastMsg(msg)
            } else {
                val user = User(postalAddress, email, firstName, lastName, phone, username)
                registerUser(email, password, user)
            }
        }
    }


    //function to ensure email and password have been successfully registered via the firebase authentication system
    private fun registerUser(emailText: String, passwordText: String, user: User) {
        // To create username and password
        auth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createUserInfo(
                        user.username!!,
                        user.firstName!!,
                        user.lastName!!,
                        user.email!!,
                        user.address!!,
                        user.phone!!
                    )

                    val msg = "Registration Successful"
                    toastMsg(msg)
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                } else {
                    //registration unsuccessful message - mainly displayed if the email given us illegitimate
                    val msg =
                        "Registration Unsuccessful - Check all the details you have entered are correct."
                    toastMsg(msg)
                }
            }
    }

    private fun toastMsg(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createUserInfo(
        username: String,
        firstName: String,
        lastName: String,
        email: String,
        address: String,
        phone: String
    ) {
        val userInfoDB = Firebase.firestore.collection("user")

        val user = User(address, email, firstName, lastName, phone, username)

        userInfoDB.add(user).addOnSuccessListener {
            Log.d(TAG, "User info add successfully")
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding user info", e)
            }
    }
}