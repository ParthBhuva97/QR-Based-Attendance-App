package com.example.attendance_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException

class Login : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    private lateinit var database: DatabaseReference

    private lateinit var sEmail: TextInputEditText
    private lateinit var sPassword : TextInputEditText
    private lateinit var btn : Button
    private lateinit var resetPassword : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        val new_user_text = findViewById<TextView>(R.id.new_user_text)

        resetPassword = findViewById(R.id.reset)

        btn = findViewById<Button>(R.id.signin_button)
        sEmail = findViewById<TextInputEditText>(R.id.userid)
        sPassword = findViewById<TextInputEditText>(R.id.password)

        resetPassword.setOnClickListener {
            val intent = Intent(this@Login,ResetPassword::class.java)
            startActivity(intent)
        }

        new_user_text.setOnClickListener{
            val intent = Intent(this@Login,CreateAccount::class.java)
            startActivity(intent)
        }

        btn.setOnClickListener{
//            val user = findViewById<TextInputEditText>(R.id.userid).text.toString()
//            val password = findViewById<TextInputEditText>(R.id.password).text.toString()
//            if(user=="Hello" && password == "123"){
//                intent = Intent(this@Login,AdminUI::class.java)
//            }
//            else{
//                intent = Intent(this@Login,StudentUI::class.java)
//            }
//            startActivity(intent)


            try{
                val email = sEmail.text.toString().trim()
                val password = sPassword.text.toString().trim()
                if(email.isEmpty() || password.isEmpty()){
                    throw IOException("Please Enter Required Information")
                }
                mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val verified = mAuth.currentUser?.isEmailVerified
                            if(verified == true){
                                val user = mAuth!!.currentUser
                                updateUI(user)
                            }
                            else{
                                Toast.makeText(this,"Please Verify Your Email Address First.",
                                    Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                this@Login, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
//                    updateUI(null)
                        }

                        // ...
                    }
            }catch(e: IOException){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Handling User Data Here
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("User").child(userId).get().addOnSuccessListener {
            val role = it.child("role").value.toString()
            when(role){
                "user" -> {
                    val intent = Intent(this@Login, StudentUI::class.java)
                    startActivity(intent)
                }
                "admin" -> {
                    val intent = Intent(this@Login, AdminUI::class.java)
                    startActivity(intent)
                }
            }

        }.addOnFailureListener{

        }

    }
}