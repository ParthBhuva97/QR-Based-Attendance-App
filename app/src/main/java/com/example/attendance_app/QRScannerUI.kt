package com.example.attendance_app

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.budiyev.android.codescanner.*
import com.example.attendance_app.R.raw.dark_done
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.scottyab.aescrypt.AESCrypt
import java.time.LocalTime


class QRScannerUI : AppCompatActivity() {
    private var maxZoom: Int = 0
    private val zoomStep = 5
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView : CodeScannerView
    private lateinit var layout_flash_container : FrameLayout
    private lateinit var image_view_flash : ImageView
    private lateinit var layout_scan_from_file_container : FrameLayout
    private lateinit var seek_bar_zoom : SeekBar
    private lateinit var button_decrease_zoom : ImageView
    private lateinit var button_increase_zoom : ImageView

    private lateinit var dataList : List<String>
    private lateinit var database: DatabaseReference


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner_ui)

        scannerView = findViewById(R.id.scanner_view)
        layout_flash_container = findViewById(R.id.layout_flash_container)
        image_view_flash = findViewById(R.id.image_view_flash)
        layout_scan_from_file_container = findViewById(R.id.layout_scan_from_file_container)
        seek_bar_zoom = findViewById(R.id.seek_bar_zoom)
        button_decrease_zoom = findViewById(R.id.button_decrease_zoom)
        button_increase_zoom = findViewById(R.id.button_increase_zoom)
        codeScanner = CodeScanner(this, scannerView)

        initScanner()
        initFlashButton()
        handleScanFromFileClicked()
        handleZoomChanged()
        handleDecreaseZoomClicked()
        handleIncreaseZoomClicked()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initScanner() {
        codeScanner.apply {
            codeScanner.camera = CodeScanner.CAMERA_BACK
            codeScanner.autoFocusMode = AutoFocusMode.SAFE
            codeScanner.formats = CodeScanner.ALL_FORMATS
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isTouchFocusEnabled = false
//            decodeCallback = DecodeCallback(::handleScannedBarcode)
//            errorCallback = ErrorCallback(::showError)

            codeScanner.decodeCallback = DecodeCallback {
                runOnUiThread {
                    val encryptedMsg = it.text
                    val key = "ASDFghjkl"
                    database = Firebase.database.reference
                    val decrypted_QRData = AESCrypt.decrypt(key, encryptedMsg)
                    dataList = decrypted_QRData.split(";")
                    val thisTime = LocalTime.now()
                    val givenTime = dataList[3]
                    val studentTime = thisTime.toString().split(":")
                    val facultyTime = givenTime.split(":")

                    if(studentTime[0] == facultyTime[0] && (studentTime[1].toInt() - facultyTime[1].toInt() < 2)){
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        database.child("User").child(userId).get()
                            .addOnSuccessListener { it ->
                                val name = it.child("firstname").value.toString() + " " + it.child("lastname").value.toString()
                                val rollNo = it.child("rollNo").value.toString()

                                val student = StudentDataModel(name,rollNo)
                                val mDialogView = LayoutInflater.from(this@QRScannerUI).inflate(R.layout.verification_layout,null)
                                val mBuilder = AlertDialog.Builder(this@QRScannerUI)
                                    .setView(mDialogView)
                                    .setTitle("Verification")
                                val mAlertDialog = mBuilder.show()
                                val verificationBtn = mDialogView.findViewById<Button>(R.id.verificationBtn)
                                val et_code = mDialogView.findViewById<TextInputEditText>(R.id.codeInput)

                                verificationBtn.setOnClickListener{
                                    database.child("Attendance").child(dataList[2]).child("Verification").get().addOnSuccessListener {
                                        val code = it.child("code").value.toString()
                                        val valid = it.child("valid").value.toString()
                                        if(valid == "true"){
                                            if(et_code.text.toString()==code){
                                                database.child("Attendance").child(dataList[2]).child(dataList[1]).child(dataList[0]).child(userId).setValue(student)
                                                mAlertDialog.dismiss()
                                                val dialogView = LayoutInflater.from(this@QRScannerUI).inflate(R.layout.marked_animation,null)
                                                val builder = AlertDialog.Builder(this@QRScannerUI)
                                                    .setView(dialogView)
                                                val alertDialog = builder.show().apply{
                                                    window?.setBackgroundDrawable(null)
                                                }
                                                val done = dialogView.findViewById<LottieAnimationView>(R.id.done)
                                                if(isDarkModeOn()){
                                                    done.setAnimation(dark_done)
                                                }
                                                Handler(Looper.getMainLooper()).postDelayed(
                                                    {
                                                        alertDialog.dismiss()
                                                    },
                                                    3000
                                                )

                                            }
                                            else{
                                                Toast.makeText(this@QRScannerUI, "Invalid Verification Code", Toast.LENGTH_SHORT).show()

                                            }
                                        }
                                        else{
                                            Toast.makeText(this@QRScannerUI, "Verification Code is Expired.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                    }
                    else{
                        Toast.makeText(this@QRScannerUI,"QR Code has Expired",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }
        }
    }

    private fun initFlashButton() {
        layout_flash_container.setOnClickListener {
            toggleFlash()
        }
        image_view_flash.isActivated = true
    }

    private fun toggleFlash() {
        image_view_flash.isActivated = image_view_flash.isActivated.not()
        codeScanner.isFlashEnabled = codeScanner.isFlashEnabled.not()
    }

    private fun handleScanFromFileClicked() {
        layout_scan_from_file_container.setOnClickListener {
            navigateToScanFromFileScreen()
        }
    }

    private fun navigateToScanFromFileScreen() {
//        ScanBarcodeFromFileActivity.start(requireActivity())
//        startActivity(Intent(this@Alt_Scanner,ScanQRFromFileActivity::class.java))
    }

    private fun handleZoomChanged() {
        seek_bar_zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    codeScanner.zoom = progress
                }
            }
        })
    }

    private fun handleDecreaseZoomClicked() {
        button_decrease_zoom.setOnClickListener {
            decreaseZoom()
        }
    }

    private fun decreaseZoom() {
        codeScanner.apply {
            if (zoom > zoomStep) {
                zoom -= zoomStep
            } else {
                zoom = 0
            }
            seek_bar_zoom.progress = zoom
        }
    }

    private fun handleIncreaseZoomClicked() {
        button_increase_zoom.setOnClickListener {
            increaseZoom()
        }
    }

    private fun increaseZoom() {
        codeScanner.apply {
            if (zoom < maxZoom - zoomStep) {
                zoom += zoomStep
            } else {
                zoom = maxZoom
            }
            seek_bar_zoom.progress = zoom
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

}