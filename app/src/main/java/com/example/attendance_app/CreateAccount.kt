package com.example.attendance_app

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException

class CreateAccount : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sEmail: TextInputEditText
    private lateinit var sPassword : TextInputEditText
    private lateinit var sCPassword : TextInputEditText
    private lateinit var sFirstName : TextInputEditText
    private lateinit var sLastName : TextInputEditText
    private lateinit var sRollNo : TextInputEditText
    private lateinit var btn : Button

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        val existingUser = findViewById<TextView>(R.id.existing_user_text)

        existingUser.setOnClickListener{
            onBackPressed()
        }

        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        btn = findViewById(R.id.signup_btn)
        sEmail = findViewById(R.id.email_text)
        sPassword = findViewById(R.id.password_text)
        sCPassword = findViewById(R.id.confirm_password_text)
        sFirstName = findViewById(R.id.firstname_text)
        sLastName = findViewById(R.id.lastname_text)
        sRollNo = findViewById(R.id.roll_no_text)
        btn.setOnClickListener {
            val email = sEmail.text.toString().trim()
            val password = sPassword.text.toString().trim()
            val confirmPassword = sCPassword.text.toString().trim()
            try{
                if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                    throw IOException("Please Enter Required Information")
                }
                val mDialogView = LayoutInflater.from(this@CreateAccount).inflate(R.layout.loading_animation, null)
                val mBuilder = AlertDialog.Builder(this@CreateAccount, R.style.AlertDialog_Theme)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.show().apply{
                    window?.setBackgroundDrawable(null)
                }

                val loadingText = mDialogView.findViewById<LottieAnimationView>(R.id.loadingText)

                if(isDarkModeOn()){
                    loadingText.setAnimation(R.raw.dark_loading_text)
                }
                if(password == confirmPassword){
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            this
                        ) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                mAuth.currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        Toast.makeText(this,"Please Check Your Inbox to Verify your Email.",
                                            Toast.LENGTH_SHORT).show()
                                        saveData()
                                    }
                                    ?.addOnFailureListener {
                                        Toast.makeText(this,it.toString(), Toast.LENGTH_SHORT).show()
                                    }
                                mAlertDialog.dismiss()
                                updateUI()
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(
                                    this, "Account Creation failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mAlertDialog.dismiss()
                                updateUI()
                            }

                            // ...
                        }
                }else{
                    Toast.makeText(this,"Password does not match.", Toast.LENGTH_SHORT).show()
                }
            }catch(e: IOException){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun saveData() {
        val email = sEmail.text.toString().trim()
        val firstname = sFirstName.text.toString().trim()
        val lastname = sLastName.text.toString().trim()
        val rollNo = sRollNo.text.toString().trim()
        var role = "user"
        if(rollNo == "3141592653"){
            role = "admin"
        }
        val user = UserModel(firstname, lastname, rollNo, email, role)

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("User").child(userId).setValue(user)
    }

    private fun updateUI() {
        val intent = Intent(this@CreateAccount, Login::class.java)
        startActivity(intent)
    }

    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}