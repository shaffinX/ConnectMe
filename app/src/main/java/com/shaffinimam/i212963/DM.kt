package com.shaffinimam.i212963

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DM : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dm)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val rv = findViewById<RecyclerView>(R.id.rv)
        val list = mutableListOf<Model_dm>()
        list.add(Model_dm("John Doe"))
        list.add(Model_dm("JOHN DOE"))
        list.add(Model_dm("John Doe"))
        list.add(Model_dm("JOHN DOE"))
        list.add(Model_dm("John Doe"))
        list.add(Model_dm("JOHN DOE"))
        val adapter = Adapter_dm(this,list)
        val la = LinearLayoutManager(this)
        rv.layoutManager = la
        rv.adapter = adapter

    }
    override fun onSupportNavigateUp(): Boolean {
        finish() // Close current activity
        return true
    }
}