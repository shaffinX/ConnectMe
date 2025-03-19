package com.shaffinimam.i212963

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(private val userList: List<Profiles>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val inviteButton: Button = itemView.findViewById(R.id.invite_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        // Set username
        holder.username.text = user.UserName

        // Load profile image from Base64 string
        if (user.Picture.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(user.Picture, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.profileImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // If there's an error, use the default placeholder
                holder.profileImage.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } else {
            // If no picture is available, use the default placeholder
            holder.profileImage.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // Set up invite button click listener
        holder.inviteButton.setOnClickListener {
            // Add your invite functionality here
            Toast.makeText(holder.itemView.context, "Invited ${user.UserName}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}