package com.shaffinimam.i212963

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Story : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var captureButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var flipCameraButton: ImageButton
    private lateinit var nextButton: TextView
    private lateinit var toolbar: Toolbar

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var capturedImageUri: Uri? = null
    private var isImageCaptured = false

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // New flag to avoid repeated permission checks
    private var permissionsChecked = false

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    // Single permission launcher for both camera and storage
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Mark permissions as checked to avoid repeated prompts
        permissionsChecked = true

        var cameraGranted = true
        var storageGranted = true

        // Check which permissions were granted
        permissions.entries.forEach {
            when (it.key) {
                Manifest.permission.CAMERA -> {
                    cameraGranted = it.value
                }
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    if (!it.value) storageGranted = false
                }
            }
        }

        // Start camera if permission was granted
        if (cameraGranted) {
            startCamera()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            previewView.visibility = View.GONE
            capturedImageView.visibility = View.VISIBLE
            capturedImageView.setImageURI(uri)
            capturedImageUri = uri
            isImageCaptured = true
            nextButton.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_story)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        previewView = findViewById(R.id.previewView)
        capturedImageView = findViewById(R.id.capturedImage)
        captureButton = findViewById(R.id.captureButton)
        galleryButton = findViewById(R.id.squareView)
        flipCameraButton = findViewById(R.id.circleView)
        nextButton = findViewById(R.id.callpers)
        toolbar = findViewById(R.id.toolbar)

        // Set up toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Initially hide the Next button until image is captured
        nextButton.visibility = View.GONE

        // Set up click listeners
        captureButton.setOnClickListener {
            takePhoto()
        }

        galleryButton.setOnClickListener {
            pickImageFromGallery()
        }

        flipCameraButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }

        nextButton.setOnClickListener {
            if (isImageCaptured) {
                uploadStory()
            } else {
                Toast.makeText(this, "Please capture or select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request permissions only once at startup
        checkPermissionsAndStartCamera()
    }

    // Simplified permission check that only runs once
    private fun checkPermissionsAndStartCamera() {
        // If we've already checked permissions, don't check again
        if (permissionsChecked) {
            if (hasCameraPermission()) {
                startCamera()
            }
            return
        }

        // Check if we already have permissions
        if (allPermissionsGranted()) {
            permissionsChecked = true
            startCamera()
            return
        }

        // Request permissions if not granted
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Helper method to check if all permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Helper method to check only camera permission
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Helper method to check storage permissions
    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Assume permissions are granted if we got this far
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(baseContext, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    if (savedUri != null) {
                        previewView.visibility = View.GONE
                        capturedImageView.visibility = View.VISIBLE
                        capturedImageView.setImageURI(savedUri)
                        capturedImageUri = savedUri
                        isImageCaptured = true
                        nextButton.visibility = View.VISIBLE
                    }
                }
            }
        )
    }

    private fun pickImageFromGallery() {
        // Assume permissions are granted if we got this far
        pickImageLauncher.launch("image/*")
    }

    private fun uploadStory() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val storyId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val dateCreated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Convert image to Base64
        val imageBase64 = capturedImageUri?.let { uri ->
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Resize bitmap to reduce database size
                val resizedBitmap = resizeBitmap(bitmap, 800)
                bitmapToBase64(resizedBitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }

        if (imageBase64 == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading message
        Toast.makeText(this, "Uploading story...", Toast.LENGTH_SHORT).show()

        // Create story object
        val story = HashMap<String, Any>()
        story["userId"] = userId
        story["storyId"] = storyId
        story["image"] = imageBase64
        story["dateCreated"] = dateCreated
        story["timestamp"] = timestamp

        // Upload to Firebase Realtime Database
        val storyRef = database.reference.child("stories").child(storyId)
        storyRef.setValue(story)
            .addOnSuccessListener {
                Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()

                // Set up deletion after 24 hours
                setupStoryDeletion(storyId)

                // Return to previous screen
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload story: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupStoryDeletion(storyId: String) {
        // Calculate deletion time (24 hours from now)
        val currentTime = System.currentTimeMillis()
        val deleteAfter = currentTime + (24 * 60 * 60 * 1000) // 24 hours in milliseconds

        // Store deletion time in Firebase
        val expiryRef = database.reference.child("story_expiry").child(storyId)
        expiryRef.setValue(deleteAfter)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (newWidth / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (newHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}