package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Home : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var postAdapter: PostAdapter
    private val postList = ArrayList<Post>()
    private val userMap = HashMap<String, User>()
    private lateinit var emptyView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val TAG = "HomeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated called")

        // Set up message button
        val dm = view.findViewById<ImageButton>(R.id.dm)
        dm.setOnClickListener {
            val intent = Intent(requireContext(), DM::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_posts)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Add Empty View Text
        emptyView = view.findViewById(R.id.empty_view)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Refresh triggered")
            fetchPosts()
        }

        // Initialize adapter
        postAdapter = PostAdapter(requireContext(), postList, userMap)
        recyclerView.adapter = postAdapter

        // Fetch users and posts
        Log.d(TAG, "Starting to fetch data")
        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        swipeRefreshLayout.isRefreshing = true
        userMap.clear()

        Log.d(TAG, "Fetching users from database")
        val usersRef = database.child("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "User data received. Count: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    Log.e(TAG, "No users found in database")
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "No users found"
                    swipeRefreshLayout.isRefreshing = false
                    return
                }

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    Log.d(TAG, "Processing user: $userId")

                    try {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let {
                            Log.d(TAG, "User loaded: ${it.username}")
                            // Make sure user id is set
                            it.id = userId
                            userMap[userId] = it
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user data: ${e.message}")
                        Toast.makeText(context, "Error loading user data", Toast.LENGTH_SHORT).show()
                    }
                }

                Log.d(TAG, "Total users loaded: ${userMap.size}")

                // Now that we have all users, fetch posts
                fetchPosts()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch users: ${error.message}")
                Toast.makeText(context, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
                emptyView.visibility = View.VISIBLE
                emptyView.text = "Error loading data: ${error.message}"
            }
        })
    }

    private fun fetchPosts() {
        Log.d(TAG, "Fetching posts")
        postList.clear()

        // In your Firebase structure, posts are nested under user IDs
        val allPosts = ArrayList<Post>()

        // Counter to track when we've processed all users
        var usersProcessed = 0
        val totalUsers = userMap.size

        // If there are no users, finish loading
        if (totalUsers == 0) {
            Log.e(TAG, "No users to fetch posts for")
            swipeRefreshLayout.isRefreshing = false
            emptyView.visibility = View.VISIBLE
            emptyView.text = "No users found"
            return
        }

        // Fetch posts directly from "Post" node (try this approach first)
        Log.d(TAG, "Trying to fetch posts from Post node directly")
        database.child("Posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Post data received. Root children count: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    Log.e(TAG, "No posts found in database")
                    fetchPostsFromUsers() // Try alternate approach
                    return
                }

                var postsFound = false

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    Log.d(TAG, "Found posts for user: $userId, post count: ${userSnapshot.childrenCount}")

                    for (postSnapshot in userSnapshot.children) {
                        val postId = postSnapshot.key ?: continue
                        Log.d(TAG, "Processing post: $postId")

                        try {
                            val post = postSnapshot.getValue(Post::class.java)
                            post?.let {
                                Log.d(TAG, "Post loaded: ${it.caption}")
                                // Make sure post id and user id are set
                                it.postId = postId
                                it.userId = userId
                                allPosts.add(it)
                                postsFound = true
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing post data: ${e.message}")
                        }
                    }
                }

                if (postsFound) {
                    Log.d(TAG, "Total posts loaded: ${allPosts.size}")
                    updatePostsList(allPosts)
                } else {
                    Log.e(TAG, "No valid posts found, trying alternate method")
                    fetchPostsFromUsers() // Try alternate approach
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch posts directly: ${error.message}")
                fetchPostsFromUsers() // Try alternate approach
            }
        })
    }

    private fun fetchPostsFromUsers() {
        Log.d(TAG, "Trying to fetch posts from each user")
        val allPosts = ArrayList<Post>()
        var usersProcessed = 0
        val totalUsers = userMap.size

        // Fetch posts for each user
        for (userId in userMap.keys) {
            Log.d(TAG, "Fetching posts for user: $userId")
            val postsRef = database.child("Post").child(userId)
            postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Post data received for user $userId. Posts count: ${snapshot.childrenCount}")

                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key ?: continue
                        Log.d(TAG, "Processing post: $postId")

                        try {
                            val post = postSnapshot.getValue(Post::class.java)
                            post?.let {
                                Log.d(TAG, "Post loaded: ${it.caption}")
                                // Make sure post id and user id are set
                                it.postId = postId
                                it.userId = userId
                                allPosts.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing post data: ${e.message}")
                        }
                    }

                    usersProcessed++
                    Log.d(TAG, "Processed $usersProcessed/$totalUsers users")

                    // If we've processed all users, update the UI
                    if (usersProcessed >= totalUsers) {
                        updatePostsList(allPosts)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch posts for user $userId: ${error.message}")
                    usersProcessed++

                    // If we've processed all users, update the UI even if some failed
                    if (usersProcessed >= totalUsers) {
                        updatePostsList(allPosts)
                    }
                }
            })
        }
    }

    private fun updatePostsList(allPosts: ArrayList<Post>) {
        if (allPosts.isEmpty()) {
            Log.e(TAG, "No posts found after all attempts")
            emptyView.visibility = View.VISIBLE
            emptyView.text = "No posts found"
            swipeRefreshLayout.isRefreshing = false
            return
        }

        Log.d(TAG, "Updating UI with ${allPosts.size} posts")

        // Sort posts by date created (newest first)
        try {
            allPosts.sortByDescending { it.dateCreated }
            Log.d(TAG, "Posts sorted by date")
        } catch (e: Exception) {
            Log.e(TAG, "Error sorting posts: ${e.message}")
        }

        // Update post list and notify adapter
        postList.clear()
        postList.addAll(allPosts)
        Log.d(TAG, "Final post list size: ${postList.size}")

        activity?.runOnUiThread {
            postAdapter.notifyDataSetChanged()
            emptyView.visibility = if (postList.isEmpty()) View.VISIBLE else View.GONE
            swipeRefreshLayout.isRefreshing = false

            // Log the first few posts for debugging
            for (i in 0 until minOf(3, postList.size)) {
                Log.d(TAG, "Post $i: ${postList[i].caption}, date: ${postList[i].dateCreated}")
            }
        }
    }
}