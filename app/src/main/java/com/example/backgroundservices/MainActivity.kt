package com.example.backgroundservices

import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var statusTextView: TextView
    private lateinit var btnStart: Button

    private lateinit var btnMulaiLatihan: Button
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        handleIntent(intent)
        statusTextView = findViewById(R.id.statusTextView)
        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            if (hasAllPermissions()) {
                startTrackerService()
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest,
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }

        btnMulaiLatihan = findViewById(R.id.btnMulaiLatihan)

        btnMulaiLatihan.setOnClickListener {
            if (hasAllPermissions()){
                startTrackerService()
            }else{
                ActivityCompat.requestPermissions(this, permissionsToRequest, REQUEST_CODE_PERMISSIONS)
            }
        }


    }
    private val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }else{
        arrayOf(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun hasAllPermissions(): Boolean{
        return permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startTrackerService(){
        val serviceIntent = Intent(this, TrackerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        statusTextView.text = "Background Service sedang berjalan..."
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "ACTION_LATIHAN_SELESAI") {
            val totalLangkah = intent.getIntExtra("JUMLAH_LANGKAH", 0)
            statusTextView.text = "Latihan Selesai!\nJumlah Langkah/Lompatan: $totalLangkah"
            buatEventKalender(totalLangkah)

        } else if (intent?.action == ACTION_SERVICE_STOPPED_JUMP) {
            statusTextView.text = "Background services sudah berhenti karena Anda melompat"
        }
    }

    private fun buatEventKalender(jumlahLangkah: Int) {
        val kalender = java.util.Calendar.getInstance()

        val intentKalender = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Latihan Fisik Selesai")
            putExtra(CalendarContract.Events.DESCRIPTION, "Berhasil menyelesaikan latihan hari ini dengan total $jumlahLangkah langkah/lompatan.")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, kalender.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, kalender.timeInMillis + (60 * 60 * 1000))
        }

        try {
            startActivity(intentKalender)
        } catch (e: Exception) {
            statusTextView.append("\n(Aplikasi Kalender tidak ditemukan)")
        }
    }
    companion object{
        const val ACTION_SERVICE_STOPPED_JUMP = "com.example.backgroundservices.ACTION_SERVICE_STOPPED_JUMP"
        const val REQUEST_CODE_PERMISSIONS = 101
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            var allPermissionsGranted = true
            for (result in grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted){
                val serviceIntent = Intent(this, TrackerService::class.java)
                ContextCompat.startForegroundService(this, serviceIntent)
                finish()
            } else{
                statusTextView.text = "Izin ditolak, service tidak dapat dijalankan"
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        setIntent(intent)
        handleIntent(intent)
    }
}