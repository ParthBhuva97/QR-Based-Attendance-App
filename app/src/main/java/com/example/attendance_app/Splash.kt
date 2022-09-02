package com.example.attendance_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class Splash : AppCompatActivity() {
    private lateinit var image : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        image = findViewById(R.id.logo)
        image.alpha = 0f
        image.animate().setDuration(2000).alpha(1f).withEndAction{
            startActivity(Intent(this@Splash,Login::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}