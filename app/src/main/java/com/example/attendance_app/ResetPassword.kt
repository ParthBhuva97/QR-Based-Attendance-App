package com.example.attendance_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {
    private lateinit var btn : Button
    private lateinit var email : TextInputEditText
    private lateinit var emailAddress : String
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()
        btn = findViewById(R.id.resetpassbtn)
        email = findViewById<TextInputEditText?>(R.id.Email_text)

        btn.setOnClickListener {
            emailAddress = email.text.toString()
            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"Please Check your Mail.", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                }
        }
    }
}