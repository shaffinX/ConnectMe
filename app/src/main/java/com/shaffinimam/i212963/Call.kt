package com.shaffinimam.i212963

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

class Call : AppCompatActivity() {
    private lateinit var agoraEngine: RtcEngine
    private val appId = "fe89e5de87704a8caa08c4f95fc7ee2d"
    private var channelName = ""
    private var isCaller = true
    private var uid = ""
    private var remoteUid = 0

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            remoteUid = uid
            runOnUiThread {
                Toast.makeText(this@Call, "User joined: $uid", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                Toast.makeText(this@Call, "User left", Toast.LENGTH_SHORT).show()
                leaveChannel()
            }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            runOnUiThread {
                Toast.makeText(this@Call, "Joined channel: $channel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val name = intent.getStringExtra("name")
        val pict = intent.getStringExtra("pic")
        uid = intent.getStringExtra("uid") ?: ""
        channelName = intent.getStringExtra("channelName") ?: ""
        isCaller = intent.getBooleanExtra("isCaller", true)

        findViewById<TextView>(R.id.name).text = name

        val hang = findViewById<ImageView>(R.id.hang)
        hang.setOnClickListener { leaveChannel() }

        val dp = findViewById<ImageView>(R.id.profile_img)
        if (!pict.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(pict, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                dp.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        initAgoraEngine()
        joinChannel()
    }

    private fun initAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(baseContext, appId, rtcEventHandler)
            // Configure for audio call
            agoraEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            agoraEngine.enableAudio()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing Agora: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun joinChannel() {
        // Join channel with token (use null for testing)
        agoraEngine.joinChannel(null, channelName, null, 0)
    }

    private fun leaveChannel() {
        try {
            agoraEngine.leaveChannel()

            // Clean up Firebase call data
            if (uid.isNotEmpty()) {
                if (isCaller) {
                    // If caller, clean up receiver's node
                    val receiverUid = channelName.replace("$uid-", "").replace("-$uid", "")
                    if (receiverUid.isNotEmpty()) {
                        FirebaseDatabase.getInstance().getReference("calls").child(receiverUid).removeValue()
                    }
                } else {
                    // If receiver, clean up your own node
                    FirebaseDatabase.getInstance().getReference("calls").child(uid).removeValue()
                }
            }

            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            agoraEngine.leaveChannel()
            RtcEngine.destroy()

            // Make sure to clean up Firebase data
            if (uid.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("calls").child(uid).removeValue()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
