package com.shaffinimam.i212963

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PostComplete : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var captionInput: EditText
    private lateinit var shareButton: Button
    private lateinit var closeButton: ImageButton

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var imageBase64: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_complete)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Posts")

        imageView = findViewById(R.id.postImageView)
        captionInput = findViewById(R.id.captioninput)
        shareButton = findViewById(R.id.shareButton)
        closeButton = findViewById(R.id.closebutt)

        // Get the image URI from the companion object in PostCamera
        val uri = PostCamera.tempImageUri
        if (uri != null) {
            try {
                // Load the image and convert to Base64 for Firebase
                imageBase64 = convertUriToBase64(uri)

                // Display the image in the ImageView
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)

                Log.d("PostComplete", "Successfully loaded image from URI")
            } catch (e: Exception) {
                Log.e("PostComplete", "Error loading image: ${e.message}", e)
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("PostComplete", "No image URI received")
            Toast.makeText(this, "No image received", Toast.LENGTH_SHORT).show()
        }

        closeButton.setOnClickListener {
            // Clear the temporary URI when closing
            PostCamera.tempImageUri = null
            startActivity(Intent(this, Navigation::class.java))
            finish()
        }

        shareButton.setOnClickListener {
            if (imageBase64 == null) {
                Toast.makeText(this, "No image to share", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (captionInput.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Please add a caption", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadPostToFirebase(imageBase64!!)
        }
    }

    private fun convertUriToBase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Get original dimensions
        val width = bitmap.width
        val height = bitmap.height

        // Scale down the image (create a smaller image to reduce Base64 size)
        val maxDimension = 800 // Maximum dimension (width or height)
        var scaleFactor = 1.0f

        if (width > height && width > maxDimension) {
            scaleFactor = maxDimension.toFloat() / width
        } else if (height > width && height > maxDimension) {
            scaleFactor = maxDimension.toFloat() / height
        }

        val scaledWidth = (width * scaleFactor).toInt()
        val scaledHeight = (height * scaleFactor).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        // Compress with lower quality to reduce size further
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)

        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadPostToFirebase(base64Image: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to share posts", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator
        Toast.makeText(this, "Uploading post...", Toast.LENGTH_SHORT).show()
        shareButton.isEnabled = false

        val postId = database.child(userId).push().key ?: return
        val caption = captionInput.text.toString().trim()
        val dateCreated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val postData = mapOf(
            "postId" to postId,
            "userId" to userId,
            "image" to base64Image,
            "caption" to caption,
            "dateCreated" to dateCreated
        )

        database.child(userId).child(postId).setValue(postData)
            .addOnSuccessListener {
                Log.d("PostComplete", "Post uploaded successfully")
                Toast.makeText(this, "Post shared successfully", Toast.LENGTH_SHORT).show()
                // Clear the temporary URI
                PostCamera.tempImageUri = null
                startActivity(Intent(this, Navigation::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("PostComplete", "Error uploading post: ${e.message}", e)
                Toast.makeText(this, "Failed to share post: ${e.message}", Toast.LENGTH_SHORT).show()
                shareButton.isEnabled = true
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the temporary URI when destroying
        PostCamera.tempImageUri = null
    }
}