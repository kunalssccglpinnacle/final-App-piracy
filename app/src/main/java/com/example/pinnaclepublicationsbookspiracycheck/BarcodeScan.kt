package com.example.pinnaclepublicationsbookspiracycheck

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pinnaclepublicationsbookspiracycheck.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private var isCameraStarted = false
    private var lastScannedBarcode: String? = null
    private var barcodeAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBarcodeScanner()
    }

    private fun initBarcodeScanner() {
        try {
            barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.CODE_128)
                .build()
            cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build()

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error initializing barcode scanner")
            return
        }
        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (!isCameraStarted) {
                    try {
                        cameraSource.start(binding.surfaceView.holder) // suppressed
                        isCameraStarted = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("Error starting camera")
                    }
                }
            }
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                try {
                    cameraSource.stop()
                    isCameraStarted = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("Error stopping camera")
                }
            }
        })
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                showToast("Barcode scanner has been stopped")
            }
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                for (i in 0 until barcodes.size()) {
                    val scannedBarcode = barcodes.valueAt(i).displayValue
                    if (scannedBarcode != lastScannedBarcode) {
                        addBarcode(scannedBarcode)
                        barcodeAdded = true
                        lastScannedBarcode = scannedBarcode
                        getBarcodeInfo(scannedBarcode)
                       // showToast("Scanned: $scannedBarcode")
                        binding.txtDetectedItems.text = scannedBarcode
                       binding.txtDetectedItems.visibility = View.VISIBLE


                        vibrate()
                    }
                }
            }
        })
    }
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
    @SuppressLint("SuspiciousIndentation")
//    private fun getBarcodeInfo(barcode: String){
//
//        binding.txtMessage1.text =""
//
//        if (barcode.contains("-")) {
//            searchBarr1(barcode)
//        }else {
//                val retrofit = Retrofit.Builder()
//                    .baseUrl("https://nodei.ssccglpinnacle.com/")
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(OkHttpClient.Builder().build())
//                    .build()
//                val api = retrofit.create(ApiService::class.java)
//            val call = api.getKey(barcode)
//                call.enqueue(object : Callback<GetKeyResponse> {
//                    override fun onResponse(
//                        call: Call<GetKeyResponse>,
//                        response: Response<GetKeyResponse>
//                    ) {
//                        try{if (response.isSuccessful) {
//                            val result = response.body()
//                            result?.let {
//                                showBarcodeInfo(it)
//                            }
//                        } else {
//                            showToast("Failed to get response from server")
//                        }}catch (e:Exception){
//
//                            binding.txtMessage.text = "This Barcode is not belongs to our data base"
//                        }
//                    }
//                    override fun onFailure(call: Call<GetKeyResponse>, t: Throwable) {
//                        t.printStackTrace()
//                        showToast("Failed to connect to server")
//                    }
//                })
//            }
//    }

    private fun getBarcodeInfo(barcode: String){
        binding.txtMessage1.text = ""

        if (barcode.contains("-")) {
            searchBarr1(barcode)
        } else {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://nodei.ssccglpinnacle.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder().build())
                .build()
            val api = retrofit.create(ApiService::class.java)
            val call = api.getKey(barcode)
            call.enqueue(object : Callback<GetKeyResponse> {
                override fun onResponse(
                    call: Call<GetKeyResponse>,
                    response: Response<GetKeyResponse>
                ) {
                    try {
                        if (response.isSuccessful) {
                            val result = response.body()
                            result?.let {
                                showBarcodeInfo(it)
                            }
                        } else {
                            showToast("Failed to get response from server")
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            binding.txtMessage.text = "This Barcode does not belong to our database"
                            binding.txtMessage.setTextColor(Color.RED)  // Set text color to red
                            binding.txtMessage.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<GetKeyResponse>, t: Throwable) {
                    t.printStackTrace()
                    showToast("Failed to connect to server")
                }
            })
        }
    }




    @SuppressLint("SetTextI18n")
    private fun showBarcodeInfo(apiResponse: GetKeyResponse?) {
        binding.txtMessage.text =""
        if (apiResponse != null) {
            val key = apiResponse.key
            runOnUiThread {
                binding.txtMessage.text = key
                binding.txtMessage.visibility = View.VISIBLE

                // Set text color based on key value
                if (key == "Not Verified") {
                    binding.txtMessage.setTextColor(Color.RED)
                } else {
                    // Set default text color
                    binding.txtMessage.setTextColor(Color.BLACK)
                }
            }
            // Call the second API with the key
            searchBarr1(key)
        } else {
            runOnUiThread {
                binding.txtMessage.text = "Response is null"
                binding.txtMessage.visibility = View.VISIBLE
            }
        }
    }
    private fun searchBarr1(key: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
        val api = retrofit.create(ApiService::class.java)
        val call = api.searchBarr1(SearchBarr1Request(key))
        call.enqueue(object : Callback<SearchBarr1Response> {
            override fun onResponse(call: Call<SearchBarr1Response>, response: Response<SearchBarr1Response>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    result?.let {
                        showSearchBarr1Response(it)
                    }
                } else {
                    showToast("Failed to get response from api 2")
                }
            }
            override fun onFailure(call: Call<SearchBarr1Response>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to api 2")
            }
        })
    }
//    private fun showSearchBarr1Response(response: SearchBarr1Response) {
//        binding.txtMessage1.text = ""
//        binding.txtMessage.text = ""
//        val result = response.result
//        val message = if (result == "Not Verified") {
//            "This book does not belong to any distributor"
//        } else {
//            "Belongs to : $result\n "
//        }
//        val coloredMessage = if (result == "Not Verified") {
//            SpannableString(message).apply {
//                setSpan(ForegroundColorSpan(Color.RED), 0, message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            }
//        } else {
//            SpannableString(message)
//        }
//        runOnUiThread {
//            binding.txtMessage1.text = coloredMessage
//            binding.txtMessage1.visibility = View.VISIBLE
//            // Set text color of txtMessage based on result
//            binding.txtMessage.text = response.result
//            if (result == "Not Verified") {
//                binding.txtMessage.setTextColor(Color.RED)
//            } else {
//                binding.txtMessage.setTextColor(Color.BLACK)
//            }
//            binding.txtMessage.visibility = View.VISIBLE
//        }
//    }


//    private fun showSearchBarr1Response(response: SearchBarr1Response) {
//        binding.txtMessage1.text = ""
//        binding.txtMessage.text = ""
//        val result = response.result
//        val message = if (result == "Not Verified") {
//            "This book does not belong to any distributor"
//        } else {
//            "Belongs to : $result. Verified\n"
//        }
//
//        val coloredMessage = if (result == "Not Verified") {
//            SpannableString(message).apply {
//                setSpan(ForegroundColorSpan(Color.RED), 0, message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            }
//        } else {
//            SpannableString(message).apply {
//                val verifiedIndex = message.indexOf("Verified")
//                val verifiedLength = "Verified".length
//                if (verifiedIndex != -1) { // Check if "Verified" exists in the message
//                    setSpan(ForegroundColorSpan(Color.GREEN), verifiedIndex, verifiedIndex + verifiedLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
//            }
//        }
//
//        runOnUiThread {
//            binding.txtMessage1.text = coloredMessage
//            binding.txtMessage1.visibility = View.VISIBLE
//            // Set text color of txtMessage based on result
//            binding.txtMessage.text = response.result
//            if (result == "Not Verified") {
//                binding.txtMessage.setTextColor(Color.RED)
//            } else {
//                binding.txtMessage.setTextColor(Color.BLACK)
//            }
//            binding.txtMessage.visibility = View.VISIBLE
//        }
//    }



    private fun showSearchBarr1Response(response: SearchBarr1Response) {
        binding.txtMessage1.text = ""
        binding.txtMessage.text = ""
        val result = response.result
        val message = if (result == "Not Verified") {
            "This book does not belong to any distributor"
        }
        else { ""
//            "Belongs to : $result. Verified\n"
        }

        val coloredMessage = if (result == "Not Verified") {
            SpannableString(message).apply {
                // Apply the red color span for the whole message when it indicates that the book does not belong to any distributor
                setSpan(ForegroundColorSpan(Color.RED), 0, message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else {
            SpannableString(message).apply {
                // Apply a green color span specifically to the word "Verified" if present
                val verifiedIndex = message.indexOf("Verified")
                if (verifiedIndex != -1) {
                    setSpan(ForegroundColorSpan(Color.GREEN), verifiedIndex, verifiedIndex + "Verified".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

//        runOnUiThread {
//            binding.txtMessage1.text = coloredMessage
//            binding.txtMessage1.visibility = View.VISIBLE
//            // Set the text color of txtMessage based on result
//            binding.txtMessage.text = response.result
//            if (result == "Not Verified") {
//                binding.txtMessage.setTextColor(Color.RED)
//            } else {
//                binding.txtMessage.setTextColor(Color.GREEN)
//            }
//            binding.txtMessage.visibility = View.VISIBLE
//        }

        runOnUiThread {
            binding.txtMessage1.text = coloredMessage
            binding.txtMessage1.visibility = View.VISIBLE

            // Set the message and color of txtMessage based on result
            if (result == "Not Verified") {
                binding.txtMessage.text = result
                binding.txtMessage.setTextColor(Color.RED)
            } else {
                binding.txtMessage.text = "$result. Verified" // Add "Verified" explicitly to the displayed result
                binding.txtMessage.setTextColor(Color.GREEN) // Set text color to green for verified results
            }

            binding.txtMessage.visibility = View.VISIBLE
        }


    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
  private fun addBarcode(barcode: String) {
      val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
        val api = retrofit.create(ApiService::class.java)
        val call = api.addBarcode(AddBarcodeRequest(barcode))
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Barcode added successfully!")
                    getCount(barcode)
                } else {
                    showToast("Failed to add barcode to the database")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to server barcode api")
            }
        })
    }
    private fun getCount(barcode: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
        val api = retrofit.create(ApiService::class.java)
        val call = api.getCount(CountRequest(barcode))
        call.enqueue(object : Callback<CountResponse> {
            override fun onResponse(call: Call<CountResponse>, response: Response<CountResponse>) {
                binding.txtMessage1.text = ""
                if (response.isSuccessful) {
                    val count = response.body()?.count ?: 0

                    val message = if (count <= 5) {
                        "Barcode scanned $count times "
                    } else {
                        "Barcode scanned $count times so it is doubtful"
                    }
                    val spannable = SpannableString(message)
                    if (count <= 5) {
                        spannable.setSpan(
                            ForegroundColorSpan(Color.GREEN),
                            message.indexOf(""),
                            message.indexOf("") + "".length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        spannable.setSpan(
                            ForegroundColorSpan(Color.RED),
                            message.indexOf("doubtful"),
                            message.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    runOnUiThread {
                        binding.txtCount.text = spannable
                        binding.txtCount.visibility = View.VISIBLE
                    }
                }
            }
            override fun onFailure(call: Call<CountResponse>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to server count : ${t.message}")
            }
        })
    }


//    private fun getCount(barcode: String) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://nodei.ssccglpinnacle.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(OkHttpClient.Builder().build())
//            .build()
//        val api = retrofit.create(ApiService::class.java)
//        val call = api.getCount(CountRequest(barcode))
//        call.enqueue(object : Callback<CountResponse> {
//            override fun onResponse(call: Call<CountResponse>, response: Response<CountResponse>) {
//                binding.txtMessage1.text = ""
//                if (response.isSuccessful) {
//                    val count = response.body()?.count ?: 0
//
//                    val message = if (count <= 5) {
//                        "Barcode scanned $count times. Verified"
//                    } else {
//                        "Barcode scanned $count times. Doubtful"
//                    }
//                    val spannable = SpannableString(message)
//                    if (count <= 5) {
//                        val verifiedText = "Verified"
//                        spannable.setSpan(
//                            ForegroundColorSpan(Color.GREEN),
//                            message.indexOf(verifiedText),
//                            message.indexOf(verifiedText) + verifiedText.length,
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                        )
//                    } else {
//                        val doubtfulText = "Doubtful"
//                        spannable.setSpan(
//                            ForegroundColorSpan(Color.RED),
//                            message.indexOf(doubtfulText),
//                            message.indexOf(doubtfulText) + doubtfulText.length,
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                        )
//                    }
//                    runOnUiThread {
//                        binding.txtCount.text = spannable
//                        binding.txtCount.visibility = View.VISIBLE
//                    }
//                }
//            }
//            override fun onFailure(call: Call<CountResponse>, t: Throwable) {
//                t.printStackTrace()
//                showToast("Failed to connect to server count : ${t.message}")
//            }
//        })
//    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
        isCameraStarted = false
    }
    override fun onResume() {
        super.onResume()
        initBarcodeScanner()
    }
}



