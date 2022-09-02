package com.example.attendance_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StudentUI : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_ui)
        val btn = findViewById<Button>(R.id.btn_scan)

//----------------------------------Old Horizontal Orientation Barcode Scanner ---------------------------
// Warning : Implementation may not be added in Gradle
        // Register the launcher and result handler
//        val barcodeLauncher = registerForActivityResult(
//            ScanContract()
//        ) { result: ScanIntentResult ->
//            if (result.contents == null) {
//                Toast.makeText(this@StudentUI, "Cancelled", Toast.LENGTH_LONG).show()
//            } else {
//                Toast.makeText(
//                    this@StudentUI,
//                    "Scanned: " + result.contents,
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//
//        btn.setOnClickListener {
//
//            val options = ScanOptions()
//            options.setOrientationLocked(false)
//            barcodeLauncher.launch(options)
//        }
//--------------------------------------------------------------------------------------------------------------

//---------------------------------New Code Scanner---------------------------------------------------------
        btn.setOnClickListener{
            val intent = Intent(this@StudentUI, QRScannerUI::class.java)
            startActivity(intent)
        }
    }
}