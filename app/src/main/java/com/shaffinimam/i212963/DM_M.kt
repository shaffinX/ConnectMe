package com.shaffinimam.i212963

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class DM_M : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_d_m__m, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv)
        val list = mutableListOf<Model_dm>()
        list.add(Model_dm("John Doe"))
        list.add(Model_dm("JOHN DOE"))
        list.add(Model_dm("John Doe"))
        list.add(Model_dm("JOHN DOE"))
        list.add(Model_dm("John Doe"))
        list.add(Model_dm("JOHN DOE"))

        val adapter = Adapter_dm(requireContext(), list)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

    }
}
