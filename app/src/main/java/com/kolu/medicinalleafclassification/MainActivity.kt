package com.kolu.medicinalleafclassification

import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kolu.medicinalleafclassification.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var imgBitmap: Bitmap

    private val classes = arrayOf(
        "Azadirachta indica", "Calotropis gigantea", "Centella asiatica",
        "Hibiscus rosa-sinensis", "Justicia adhatoda", "Kalanchoe pinnata",
        "Mikania micrantha", "Ocimum tenuiflorum", "Phyllanthus emblica", "Terminalia arjuna"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Toast.makeText(applicationContext,
            "app logo: Flaticon\n" +
                    "model trained: Ashik\n" +
                    "android deployment: Zisan", Toast.LENGTH_SHORT).show()

        binding.btnCamera.setOnClickListener {takeFromCamera()}
        binding.btnGallery.setOnClickListener { takeFromGallery() }
        binding.btnPredict.setOnClickListener { predict() }

    }

    fun predict(){
        if(binding.imvLoadedImage.visibility == View.INVISIBLE){
            Toast.makeText(applicationContext, "Load an image first", Toast.LENGTH_LONG).show()
            return
        }
        val runModel = RunModel()
        val imgByteBuffer = runModel.preprocess(imgBitmap)
        val confidenceArray = runModel.run(applicationContext, imgByteBuffer)
        var maxConfidence = 0f
        var maxConfidenceIndex = 0
        for (index in 0 .. 9){
            if (confidenceArray != null && confidenceArray[index] > maxConfidence){
                maxConfidence = confidenceArray[index]
                maxConfidenceIndex = index
            }
        }
        binding.tvClassOfLeave.text = classes[maxConfidenceIndex]



//        val logArray = confidenceArray?.joinToString(", ", "[", "]")
//        if (logArray != null) {
//            Log.d("FloatArrayLog", logArray)
//        }
    }

    fun takeFromCamera(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        } else{
            val cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, 1)
        }
    }

    fun takeFromGallery(){
        val galleryIntent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 2)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (data != null) {
                if (requestCode == 1 && resultCode == RESULT_OK){
                    imgBitmap = data.extras?.get("data") as Bitmap
                    binding.imvLoadedImage.setImageBitmap(imgBitmap)
                    binding.imvLoadedImage.visibility = View.VISIBLE
                } else if (requestCode == 2){
                    val dat: Uri? = data.data
                    imgBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, dat)
                    binding.imvLoadedImage.setImageBitmap(imgBitmap)
                    binding.imvLoadedImage.visibility = View.VISIBLE
                }
            }
    }
}