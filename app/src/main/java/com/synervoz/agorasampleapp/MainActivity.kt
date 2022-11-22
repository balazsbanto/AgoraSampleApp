package com.synervoz.agorasampleapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.synervoz.agorasampleapp.agora.AgoraEngine
import com.synervoz.agorasampleapp.agora.Room
import com.synervoz.agorasampleapp.agora.RoomPanel
import com.synervoz.agorasampleapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    lateinit var  binding:ActivityMainBinding
    lateinit var audioRecord: AudioRecord

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recordButton.setOnClickListener {
            startRecording()
        }

        binding.agoraButton.setOnClickListener {
            startAgora()
            audioRecord.addOnRoutingChangedListener(
                object : AudioRouting.OnRoutingChangedListener {
                    override fun onRoutingChanged(router: AudioRouting?) {
                        binding.textView.text = "ROUTING CHANGED !!!"
                        Log.d(TAG, "Routing changed")
                        val a = router?.routedDevice
                    }
                },
                null
            )
        }
    }

    private fun startAgora() {
        AgoraEngine.instantiate()
        var room = AgoraEngine.shared.createRoom("some-roomID") as Room
        room.roomInterface = RoomPanel()
        room.join("userId")
    }

    fun startRecording() {
        val AUDIO_SOURCE =
            MediaRecorder.AudioSource.VOICE_COMMUNICATION // for raw audio, use MediaRecorder.AudioSource.UNPROCESSED, see note in MediaRecorder section

        val SAMPLE_RATE = 44100
        val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO
        val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_8BIT
        val BUFFER_SIZE_RECORDING = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

        audioRecord = AudioRecord(
            AUDIO_SOURCE,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE_RECORDING
        )

        if (audioRecord.getState() !== AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "error initializing")
            throw Exception("error initializing")
        }

//        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        val inputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
//        var selectedInput : AudioDeviceInfo? = null
//        for (dev in inputDevices) {
//            if (dev.address == "back") {
//                selectedInput = dev
//                break
//            }
//        }
//
//        val b = audioRecord.setPreferredDevice(selectedInput)

        audioRecord.startRecording()
        Thread(
            Runnable {
                writeAudioData(BUFFER_SIZE_RECORDING)
            }
        ).start()
    }

    private fun writeAudioData(bufferSizeRecording: Int) { // to be called in a Runnable for a Thread created after call to startRecording()
        val recordedFile = File(this.filesDir, "recording.wav")
        val data =
            ByteArray(bufferSizeRecording / 2) // assign size so that bytes are read in in chunks inferior to AudioRecord internal buffer size
        var outputStream: FileOutputStream? = null
        try {
            outputStream =
                FileOutputStream(recordedFile) // fileName is path to a file, where audio data should be written
        } catch (e: FileNotFoundException) {
            // handle error
        }
        while (true) { // continueRecording can be toggled by a button press, handled by the main (UI) thread
            val read: Int = audioRecord.read(data, 0, data.size)
            try {
                outputStream?.write(data, 0, read)
            } catch (e: Exception) {
                Log.d(TAG, "exception while writing to file")
                e.printStackTrace()
                break
            }
        }

        try {
            outputStream?.flush()
            outputStream?.close()
        } catch (e: IOException) {
            Log.d(TAG, "exception while closing output stream " + e.toString())
            e.printStackTrace()
        }

        // Clean up
        audioRecord.stop()
        audioRecord.release()
    }

    private fun requestPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permissions, 0)
                return false
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != 0 || grantResults.isEmpty() || grantResults.size != permissions.size) return
        var hasAllPermissions = true

        for (grantResult in grantResults)
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
                Toast.makeText(
                    applicationContext,
                    "Please allow all permissions for the app.",
                    Toast.LENGTH_LONG
                ).show()
            }

//        if (hasAllPermissions) initialize()
    }
}