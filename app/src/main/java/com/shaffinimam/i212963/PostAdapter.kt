package com.shaffinimam.i212963
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val context: Context,
    private val postList: ArrayList<Post>,
    private val userMap: HashMap<String, User>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val TAG = "PostAdapter"
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        Log.d(TAG, "Creating new view holder")
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${postList.size}")
        return postList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        Log.d(TAG, "Binding post at position $position: ${post.caption}")

        // Get user info for this post
        val user = userMap[post.userId]
        Log.d(TAG, "User for post: ${user?.username ?: "Unknown"}")

        // Set username
        holder.username.text = user?.username ?: "Unknown User"

        // Load post image from Base64
        if (post.image.isNotEmpty()) {
            Log.d(TAG, "Post has image, attempting to decode")
            try {
                val imageBytes = Base64.decode(post.image, Base64.DEFAULT)
                Log.d(TAG, "Image decoded, byte length: ${imageBytes.size}")
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (decodedImage != null) {
                    Log.d(TAG, "Image bitmap created successfully")
                    holder.postImage.setImageBitmap(decodedImage)
                } else {
                    Log.e(TAG, "Failed to create bitmap from decoded bytes")
                    holder.postImage.setImageResource(R.drawable.post)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding image: ${e.message}")
                holder.postImage.setImageResource(R.drawable.post)
            }
        } else {
            Log.e(TAG, "Post has no image data")
            holder.postImage.setImageResource(R.drawable.post)
        }

        // Set caption
        holder.caption.text = post.caption
        Log.d(TAG, "Caption set: ${post.caption}")

        // Set like count
        holder.likeCount.text = if (post.likesCount == 1) "1 like" else "${post.likesCount} likes"
        Log.d(TAG, "Like count set: ${post.likesCount}")

        // Check if current user has liked this post
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Checking if user ${currentUser.uid} liked post ${post.postId}")
            checkIfLiked(post.postId, currentUser.uid) { isLiked ->
                Log.d(TAG, "Like status: $isLiked")
                holder.likeButton.isChecked = isLiked
            }
        } else {
            Log.d(TAG, "No current user logged in")
        }

        // Like button click listener
        holder.likeButton.setOnClickListener {
            currentUser?.let { user ->
                Log.d(TAG, "Like button clicked, new state: ${holder.likeButton.isChecked}")
                val isLiked = holder.likeButton.isChecked
                updateLike(post.postId, user.uid, isLiked)
            } ?: run {
                Log.e(TAG, "Cannot like - user not logged in")
                Toast.makeText(context, "You need to be logged in to like posts", Toast.LENGTH_SHORT).show()
            }
        }

        // Comment button click listener
        holder.commentButton.setOnClickListener {
            Log.d(TAG, "Comment button clicked")
            Toast.makeText(context, "Comments coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Share button click listener
        holder.shareButton.setOnClickListener {
            Log.d(TAG, "Share button clicked")
            Toast.makeText(context, "Share feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfLiked(postId: String, userId: String, callback: (Boolean) -> Unit) {
        val likeRef = database.child("likes").child(postId).child(userId)
        likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Like check result: ${snapshot.exists()}")
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error checking like status: ${error.message}")
                callback(false)
            }
        })
    }

    private fun updateLike(postId: String, userId: String, isLiked: Boolean) {
        Log.d(TAG, "Updating like: postId=$postId, userId=$userId, isLiked=$isLiked")
        val likeRef = database.child("likes").child(postId).child(userId)
        val postRef = database.child("Post").child(userId).child(postId)

        if (isLiked) {
            // Add like
            likeRef.setValue(true).addOnSuccessListener {
                Log.d(TAG, "Like added successfully")
                // Update like count in post
                postRef.child("likesCount").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLikes = snapshot.getValue(Int::class.java) ?: 0
                        Log.d(TAG, "Current likes: $currentLikes, updating to ${currentLikes + 1}")
                        postRef.child("likesCount").setValue(currentLikes + 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error updating like count: ${error.message}")
                    }
                })
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error adding like: ${e.message}")
            }
        } else {
            // Remove like
            likeRef.removeValue().addOnSuccessListener {
                Log.d(TAG, "Like removed successfully")
                // Update like count in post
                postRef.child("likesCount").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLikes = snapshot.getValue(Int::class.java) ?: 0
                        if (currentLikes > 0) {
                            Log.d(TAG, "Current likes: $currentLikes, updating to ${currentLikes - 1}")
                            postRef.child("likesCount").setValue(currentLikes - 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error updating like count: ${error.message}")
                    }
                })
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error removing like: ${e.message}")
            }
        }
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.iv_profile_picture)
        val username: TextView = itemView.findViewById(R.id.tv_username)
        val postImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        val likeButton: ToggleButton = itemView.findViewById(R.id.btn_like)
        val commentButton: ImageView = itemView.findViewById(R.id.btn_comment)
        val shareButton: ImageView = itemView.findViewById(R.id.btn_share)
        val likeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        val caption: TextView = itemView.findViewById(R.id.tv_caption)
    }
}