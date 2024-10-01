package org.d3if3163.scanapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import org.d3if3163.scanapp.R
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var cameraView: SurfaceView
    private lateinit var productInfo: TextView
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Pastikan R.layout.activity_main mengarah ke layout Anda

        val scanButton = findViewById<Button>(R.id.scan_button)
        productInfo = findViewById(R.id.product_info)
        cameraView = findViewById(R.id.camera_view)

        // Inisialisasi barcode detector
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(640, 480)
            .setAutoFocusEnabled(true)
            .build()

        scanButton.setOnClickListener { startCamera() }

        // Set up barcode detector processor
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val barcodeData = barcodes.valueAt(0).displayValue
                    runOnUiThread { productInfo.text = "Product Code: $barcodeData" }
                }
            }
        })
    }

    private fun startCamera() {
        // Cek apakah izin kamera telah diberikan
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jika belum, minta izin kamera
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
            return
        }

        try {
            // Mulai kamera jika izin sudah diberikan
            cameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraSource.start(cameraView.holder)
                        } else {
                            // Jika izin tidak ada, tampilkan pesan
                            Toast.makeText(
                                this@MainActivity,
                                "Camera permission is required to use this feature.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
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
                    cameraSource.stop()
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            // Jika permintaan izin untuk kamera disetujui, mulai kamera
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                // Jika tidak disetujui, tampilkan pesan
                Toast.makeText(
                    this,
                    "Camera permission is required to use this feature.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

