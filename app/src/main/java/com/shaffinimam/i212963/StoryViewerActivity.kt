package com.shaffinimam.i212963

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StoryViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageView = ImageView(this)
        imageView.layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        setContentView(imageView)

        val encodedImage = intent.getStringExtra("storyImage")
        if (encodedImage != null) {
            val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 15000)
    }
}
