package com.shaffinimam.i212963

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class EditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var profileImage: CircleImageView
    private lateinit var editName: TextView
    private lateinit var editUsername: TextView
    private lateinit var editContact: TextView
    private lateinit var displayName: TextView
    private lateinit var btnDone: TextView
    private lateinit var editBio: TextView
    private var imageUri: Uri? = null
    private var encodedImage: String? = null
    private val userId get() = auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Profile").child(userId)

        // Check if user is logged in
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI
        profileImage = findViewById(R.id.profileImage)
        editName = findViewById(R.id.editName)
        editUsername = findViewById(R.id.editUsername)
        editContact = findViewById(R.id.editContact) // Ensure you have this field in XML
        displayName = findViewById(R.id.displayName) // This will now show Name from Firebase
        btnDone = findViewById(R.id.btnDone)
        editBio= findViewById(R.id.editBio) // Ensure you have this field in XML

        // Load Profile Data
        loadProfileData()

        // Set Listeners
        profileImage.setOnClickListener { checkPermissionsAndPickImage() }
        btnDone.setOnClickListener { saveProfileData() }
    }

    private fun loadProfileData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("Name").getValue(String::class.java) ?: ""
                    val username = snapshot.child("UserName").getValue(String::class.java) ?: ""
                    val contact = snapshot.child("Contact").getValue(String::class.java) ?: ""
                    val bio = snapshot.child("Bio").getValue(String::class.java) ?: ""

                    editName.text = name
                    editUsername.text = username
                    editContact.text = contact // Ensure this field is being displayed
                    displayName.text = name // Update displayName to show Name
                    editBio.text = bio


                    val imageString = snapshot.child("Picture").getValue(String::class.java)
                    imageString?.let {
                        val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                        profileImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkPermissionsAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            profileImage.setImageBitmap(bitmap)
            encodedImage = encodeImage(bitmap)
        }
    }

    private fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun saveProfileData() {
        val name = editName.text.toString().trim()
        val username = editUsername.text.toString().trim()
        val contact = editContact.text.toString().trim()
        val bio = editBio.text.toString().trim()

        if (name.isEmpty() || username.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val profileMap = hashMapOf<String, Any>(
            "Name" to name,
            "UserName" to username,
            "Contact" to contact,
            "Bio" to bio
        )

        encodedImage?.let { profileMap["Picture"] = it }

        database.updateChildren(profileMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                loadProfileData() // Refresh UI after update
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
