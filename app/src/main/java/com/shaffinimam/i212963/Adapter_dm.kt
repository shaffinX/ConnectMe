package com.shaffinimam.i212963

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class Adapter_dm(val context: Context, val list: MutableList<Model_dm>) : RecyclerView.Adapter<Adapter_dm.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val imageView: CircleImageView = itemView.findViewById(R.id.profile_img)
        val button: LinearLayout = itemView.findViewById(R.id.clickbutto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_dm, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = list[position]
        holder.name.text = user.name

        // Decode base64 image
        if (user.picture.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(user.picture, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Invalid image
            }
        }

        holder.button.setOnClickListener {
            val intent = Intent(context, DM2::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("pic", user.picture)
            context.startActivity(intent)
        }
    }
}
