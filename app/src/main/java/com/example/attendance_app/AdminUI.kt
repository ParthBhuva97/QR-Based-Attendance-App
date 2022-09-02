package com.example.attendance_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AdminUI : AppCompatActivity() {
    private lateinit var btncheckAttendance : Button
    private lateinit var btntakeAttendance : Button
    private lateinit var tvgreeting : TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_ui)

        database = Firebase.database.reference
        btntakeAttendance = findViewById(R.id.qrgen)
        btncheckAttendance = findViewById(R.id.attendanceCheck)
        tvgreeting = findViewById(R.id.greeting)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("User").child(userId).get()
            .addOnSuccessListener {
                val name = it.child("firstname").value.toString() + " " + it.child("lastname").value.toString()
                tvgreeting.text = "Welcome, $name"
            }
        btntakeAttendance.setOnClickListener {
            val intent = Intent(this@AdminUI,QRGeneratorUI::class.java)
            startActivity(intent)
        }

        btncheckAttendance.setOnClickListener {
            Toast.makeText(this,"Feature is not available in your Region", Toast.LENGTH_SHORT).show()
        }
    }
}