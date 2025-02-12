package com.shaffinimam.i212963

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter_dm(val context:Context,val list: MutableList<Model_dm>) : RecyclerView.Adapter<Adapter_dm.MyViewHolder>() {
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name: TextView =itemView.findViewById(R.id.name) as TextView

        val button:LinearLayout = itemView.findViewById(R.id.clickbutto) as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_dm,parent,false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = list[position].name
        holder.button.setOnClickListener{
            val intent = Intent(context,DM2::class.java)
            intent.putExtra("name",list[position].name)
            context.startActivity(intent);
        }
    }
}