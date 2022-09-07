package com.example.attendance_app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.scottyab.aescrypt.AESCrypt
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalTime

class QRGeneratorUI : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adminId : String
    private lateinit var role:String
    private lateinit var QR_Data:String
    private lateinit var dateToday : String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrgenerator_ui)

        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        val qrBtn = findViewById<Button>(R.id.qrButton)
        val image = findViewById<ImageView>(R.id.image)
        val key = "ASDFghjkl"
        val codeBtn = findViewById<Button>(R.id.codeButton)
        qrBtn.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            database.child("User").child(userId).get().addOnSuccessListener {
                adminId = it.child("firstname").value.toString()
                val date = findViewById<TextInputEditText>(R.id.date)
//                date.setOnClickListener {
//                    val mDialogView = LayoutInflater.from(this@QRGeneratorUI).inflate(R.layout.datepicker,null)
//                    val mBuilder = AlertDialog.Builder(this@QRGeneratorUI).setView(mDialogView)
//                    val datePicker = mDialogView.findViewById<DatePicker>(R.id.datePicker)
//                    val today = Calendar.getInstance()
//
//                    val mAlertDialog = mBuilder.show()
//                    datePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
//                        today.get(Calendar.DAY_OF_MONTH)
//
//                    ) { view, year, month, day ->
//                        val month = month + 1
//                        dateToday = "$day-$month-$year"
//                    }
//                    date.setText(dateToday)
//                    mAlertDialog.dismiss()
//                }
                val classname = findViewById<TextInputEditText>(R.id.department)
                val time = LocalTime.now()
                try{
                    if(date.text.toString().isEmpty() || classname.text.toString().isEmpty()){
                        throw IOException("Enter Required Input")
                    }
                    QR_Data = date.text.toString().trim() + ";" + classname.text.toString().trim() + ";" + adminId + ";" + time.toString()

                    val encrypted_QRData = AESCrypt.encrypt(key, QR_Data)
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(encrypted_QRData, BarcodeFormat.QR_CODE, 1024, 1024)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                        }
                    }
                    image.setImageBitmap(bitmap)
                    image.setOnClickListener{
                        shareImageandText(bitmap)
                    }
                }catch(e: IOException){
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                }


            }.addOnFailureListener{

            }

        }
        codeBtn.setOnClickListener {
            val randomCode = getRandomString(5)
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val time = LocalTime.now()
            val verify = VerificationModel(true, randomCode)
            database.child("User").child(userId).get()
                .addOnSuccessListener {
                    adminId = it.child("firstname").value.toString()
                    database.child("Attendance").child(adminId).child("Verification").setValue(verify)
                    val mDialogView = LayoutInflater.from(this@QRGeneratorUI).inflate(R.layout.v_code,null)
                    val mBuilder = AlertDialog.Builder(this@QRGeneratorUI).
                    setView(mDialogView)
                    val doneBtn = mDialogView.findViewById<Button>(R.id.okBtn)
                    val codeView = mDialogView.findViewById<TextView>(R.id.code)

                    codeView.text = randomCode
                    val mAlertDialog = mBuilder.show()
                    val counter = mDialogView.findViewById<TextView>(R.id.counter)
                    object : CountDownTimer(15000, 1000) {

                        // Callback function, fired on regular interval
                        override fun onTick(millisUntilFinished: Long) {
                            counter.text = "Seconds remaining: ${millisUntilFinished / 1000}"
                        }

                        // Callback function, fired
                        // when the time is up
                        override fun onFinish() {
                            counter.text = "Code Expired."
                            database.child("Attendance").child(adminId).child("Verification").child("valid").setValue(false)
                        }
                    }.start()

                    doneBtn.setOnClickListener {
                        mAlertDialog.dismiss()
                    }

                }

        }
    }
    private fun shareImageandText(bitmap: Bitmap) {
        val uri: Uri? = getmageToShare(bitmap)
        val intent = Intent(Intent.ACTION_SEND)

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        // adding text to share
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing Image")

        // Add subject Here
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

        // setting type to image
        intent.type = "image/png"

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, "Share Via"))
    }
    private fun getmageToShare(bitmap: Bitmap): Uri? {
        val imagefolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imagefolder.mkdirs()
            val file = File(imagefolder, "shared_image.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(this, "com.example.capstoneproject", file)
        } catch (e: Exception) {
            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }

    private fun getRandomString(length: Int) : String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }
}