package com.example.attendance_app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.example.attendance_app.R.raw.dark_scanner

class StudentUI : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_ui)
        val btn = findViewById<Button>(R.id.btn_scan)
        val scanAnimation = findViewById<LottieAnimationView>(R.id.scanAnimation)

        if(isDarkModeOn()){
            scanAnimation.setAnimation(dark_scanner)
        }



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
            getPermission()
        }
    }

    private fun getPermission(){
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report.let {
                        if(report!!.areAllPermissionsGranted()){
                            val intent = Intent(this@StudentUI, QRScannerUI::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this@StudentUI, "Camera Permission Required.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener{
                Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            }.check()
    }

    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}