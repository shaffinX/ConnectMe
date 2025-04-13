package com.shaffinimam.i212963

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Home : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var storyRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var postAdapter: PostAdapter
    private lateinit var storyAdapter: StoryAdapter
    private val postList = ArrayList<Post>()
    private val storyList = ArrayList<Stories>()
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dm = view.findViewById<ImageButton>(R.id.dm)
        dm.setOnClickListener {
            val intent = Intent(requireContext(), DM::class.java)
            startActivity(intent)
        }

        recyclerView = view.findViewById(R.id.recycler_view_posts)
        recyclerView.layoutManager = LinearLayoutManager(context)

        emptyView = view.findViewById(R.id.empty_view)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener { fetchPosts() }

        postAdapter = PostAdapter(requireContext(), postList, userMap)
        recyclerView.adapter = postAdapter

        // Setup story recycler
        storyRecyclerView = view.findViewById(R.id.recycler_view_stories)
        storyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        Log.d(TAG, "Setting up StoryAdapter")
        storyAdapter = StoryAdapter(storyList, userMap) { story ->
            Log.d(TAG, "Story clicked callback triggered")
            val intent = Intent(requireContext(), StoryViewerActivity::class.java)
            intent.putExtra("storyImage", story.image)
            startActivity(intent)
        }
        storyRecyclerView.adapter = storyAdapter

        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        Log.d(TAG, "Fetching all users")
        swipeRefreshLayout.isRefreshing = true
        userMap.clear()

        val usersRef = database.child("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "User data snapshot received, count: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    Log.d(TAG, "No users found in database")
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "No users found"
                    swipeRefreshLayout.isRefreshing = false
                    return
                }

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    val user = userSnapshot.getValue(User::class.java)

                    Log.d(TAG, "Processing user with ID: $userId")

                    // Check if Profile object exists for this user
                    val profileSnapshot = userSnapshot.child("Profile")
                    if (profileSnapshot.exists()) {
                        Log.d(TAG, "Profile found for user $userId")

                        // Get profile picture from Profile object
                        val profilePicture = profileSnapshot.child("Picture").getValue(String::class.java)
                        Log.d(TAG, "Profile picture found: ${profilePicture != null}")

                        user?.let {
                            it.id = userId
                            // Set profile picture from Profile object
                            it.picture = profilePicture ?: ""
                            userMap[userId] = it

                            Log.d(TAG, "Loaded user: ${it.username}")
                            Log.d(TAG, "Picture is empty: ${it.picture.isEmpty()}")
                            if (it.picture.isNotEmpty()) {
                                Log.d(TAG, "Picture length: ${it.picture.length}")
                                try {
                                    val imageBytes = Base64.decode(it.picture, Base64.DEFAULT)
                                    Log.d(TAG, "Successfully decoded image, bytes: ${imageBytes.size}")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to decode user picture", e)
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "No Profile object found for user $userId")
                        user?.let {
                            it.id = userId
                            userMap[userId] = it
                        }
                    }
                }

                Log.d(TAG, "Loaded ${userMap.size} users, now fetching posts and stories")
                fetchPosts()
                fetchStories()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error when fetching users: ${error.message}")
                swipeRefreshLayout.isRefreshing = false
                emptyView.visibility = View.VISIBLE
                emptyView.text = "Error loading users: ${error.message}"
            }
        })
    }

    // Rest of the methods remain the same
    private fun fetchStories() {
        Log.d(TAG, "Fetching stories")
        database.child("stories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Story data snapshot received, count: ${snapshot.childrenCount}")
                storyList.clear()

                for (storySnap in snapshot.children) {
                    val story = storySnap.getValue(Stories::class.java)
                    story?.let {
                        Log.d(TAG, "Loading story ID: ${it.storyId}, from user: ${it.userId}")

                        // Check if user exists in userMap
                        if (userMap.containsKey(it.userId)) {
                            Log.d(TAG, "User found for this story: ${userMap[it.userId]?.username}")
                        } else {
                            Log.d(TAG, "No user found for userID: ${it.userId}")
                        }

                        storyList.add(it)
                    }
                }

                Log.d(TAG, "Loaded ${storyList.size} stories, notifying adapter")
                activity?.runOnUiThread {
                    storyAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch stories: ${error.message}")
            }
        })
    }

    private fun fetchPosts() {
        Log.d(TAG, "Fetching posts")
        postList.clear()

        val allPosts = ArrayList<Post>()
        val totalUsers = userMap.size

        if (totalUsers == 0) {
            Log.d(TAG, "No users loaded, cannot fetch posts")
            swipeRefreshLayout.isRefreshing = false
            emptyView.visibility = View.VISIBLE
            emptyView.text = "No users found"
            return
        }

        database.child("Posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Post data snapshot received, exists: ${snapshot.exists()}")

                if (!snapshot.exists()) {
                    Log.d(TAG, "No posts found in 'Posts' node, trying 'Post' node")
                    fetchPostsFromUsers()
                    return
                }

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    Log.d(TAG, "Processing posts for user: $userId")

                    for (postSnapshot in userSnapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let {
                            it.postId = postSnapshot.key ?: ""
                            it.userId = userId
                            Log.d(TAG, "Added post ID: ${it.postId}")
                            allPosts.add(it)
                        }
                    }
                }

                updatePostsList(allPosts)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error when fetching posts: ${error.message}")
                fetchPostsFromUsers()
            }
        })
    }

    private fun fetchPostsFromUsers() {
        Log.d(TAG, "Fetching posts from users' individual nodes")
        val allPosts = ArrayList<Post>()
        var usersProcessed = 0
        val totalUsers = userMap.size

        for (userId in userMap.keys) {
            val postsRef = database.child("Post").child(userId)
            Log.d(TAG, "Fetching posts for user: $userId")

            postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Post data for user $userId, count: ${snapshot.childrenCount}")

                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let {
                            it.postId = postSnapshot.key ?: ""
                            it.userId = userId
                            Log.d(TAG, "Added post ID: ${it.postId} for user: $userId")
                            allPosts.add(it)
                        }
                    }
                    usersProcessed++
                    Log.d(TAG, "Processed $usersProcessed of $totalUsers users")

                    if (usersProcessed >= totalUsers) {
                        Log.d(TAG, "All users processed, updating posts list")
                        updatePostsList(allPosts)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error fetching posts for user $userId: ${error.message}")
                    usersProcessed++

                    if (usersProcessed >= totalUsers) {
                        Log.d(TAG, "All users processed (with some errors), updating posts list")
                        updatePostsList(allPosts)
                    }
                }
            })
        }
    }

    private fun updatePostsList(allPosts: ArrayList<Post>) {
        Log.d(TAG, "Updating posts list with ${allPosts.size} posts")

        if (allPosts.isEmpty()) {
            Log.d(TAG, "No posts found to display")
            emptyView.visibility = View.VISIBLE
            emptyView.text = "No posts found"
            swipeRefreshLayout.isRefreshing = false
            return
        }

        allPosts.sortByDescending { it.dateCreated }
        postList.clear()
        postList.addAll(allPosts)

        activity?.runOnUiThread {
            Log.d(TAG, "Notifying post adapter of data change")
            postAdapter.notifyDataSetChanged()
            emptyView.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }
}