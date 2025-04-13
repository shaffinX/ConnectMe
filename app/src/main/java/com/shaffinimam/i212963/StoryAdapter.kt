package com.shaffinimam.i212963
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(
    private val stories: List<Stories>,
    private val users: Map<String, User>,
    private val onStoryClick: (Stories) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.story_profile_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        val user = users[story.userId]

        // Debug logs
        Log.d("StoryAdapter", "Binding story at position: $position")
        Log.d("StoryAdapter", "Story userId: ${story.userId}")
        Log.d("StoryAdapter", "User found: ${user != null}")
        user?.let {
            Log.d("StoryAdapter", "Username: ${it.username}")
            Log.d("StoryAdapter", "Has picture: ${it.picture != null && it.picture.isNotEmpty()}")
            if (it.picture != null && it.picture.isNotEmpty()) {
                Log.d("StoryAdapter", "Picture length: ${it.picture.length}")
            }
        }

        // Set default image first
        holder.profileImage.setImageResource(R.drawable.ic_profile_placeholder)

        // Try to set user profile picture if available
        if (user?.picture != null && user.picture.isNotEmpty()) {
            try {
                Log.d("StoryAdapter", "Attempting to decode image")
                val imageBytes = Base64.decode(user.picture, Base64.DEFAULT)
                Log.d("StoryAdapter", "Decoded byte array length: ${imageBytes.size}")
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap != null) {
                    Log.d("StoryAdapter", "Bitmap created successfully: ${bitmap.width}x${bitmap.height}")
                    holder.profileImage.setImageBitmap(bitmap)
                } else {
                    Log.e("StoryAdapter", "Failed to create bitmap from bytes")
                }
            } catch (e: Exception) {
                Log.e("StoryAdapter", "Error decoding profile image", e)
            }
        } else {
            Log.d("StoryAdapter", "No profile picture available for this user")
        }

        holder.itemView.setOnClickListener {
            onStoryClick(story)
        }
    }

    override fun getItemCount(): Int = stories.size
}