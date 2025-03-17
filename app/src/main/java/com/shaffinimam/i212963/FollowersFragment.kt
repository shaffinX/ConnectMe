package com.shaffinimam.i212963

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FollowersFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv)
        val list = mutableListOf<Model_Followers>()
        list.add(Model_Followers("John Doe"))
        list.add(Model_Followers("JOHN DOE"))
        list.add(Model_Followers("John Doe"))
        list.add(Model_Followers("JOHN DOE"))
        list.add(Model_Followers("John Doe"))
        list.add(Model_Followers("JOHN DOE"))
        list.add(Model_Followers("Jane DOE"))
        val adapter = Adapter_Followers(requireContext(),list)
        val la = LinearLayoutManager(requireContext())
        rv.layoutManager = la
        rv.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_followers, container, false)
    }


}