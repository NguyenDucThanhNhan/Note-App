package com.ltdd.a7_noteapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ltdd.a7_noteapp.databinding.ActivityMainBinding
import com.ltdd.a7_noteapp.model.Post

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        auth = Firebase.auth

        database = Firebase.database
        myRef = database.getReference("message")
        firestore = Firebase.firestore

        binding.rvNotes.layoutManager = GridLayoutManager(
            this,
            2,
            RecyclerView.VERTICAL,
            false
        )

        addNotes()
    }

    fun addNotes(){
        val id = myRef.push().key
        val title = "Test Title"
        val content = "Test content"

        if (id != null) {
            myRef.child(id).setValue(Post(id, title, content)).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DEBUG", "post data successful")
                } else {
                    Log.d("DEBUG", "post data unsuccessful")
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(myRef,Post::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Post, PostHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
                return PostHolder(
                    LayoutInflater.from(parent.context)
                    .inflate(R.layout.note_items, parent, false))
            }

            protected override fun onBindViewHolder(holder: PostHolder, position: Int, model: Post) {
                holder.txtTitle.text = model.title
                holder.txtContent.text = model.content
            }

            override fun onDataChanged() {
            }
        }

        binding.rvNotes.adapter = adapter
        adapter.startListening()
    }

    class PostHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtTitle: TextView
        var txtContent: TextView

        init {
            txtTitle = view.findViewById(R.id.txt_title)
            txtContent = view.findViewById(R.id.txt_content)
        }
    }


    fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    Log.d("DEBUG", "Login successful")
                } else {
                    Log.w("DEBUG", "Login fail", task.exception)
                }
            }
    }

    fun createNewUser(newEmail: String, newPassword: String){
        auth.createUserWithEmailAndPassword(newEmail, newPassword)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    Log.d("DEBUG", "Create new user successful")
                } else {
                    Log.w("DEBUG", "Create new user fail", task.exception)
                }
            }
    }

    fun resetPassword(email: String){
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) {
                    Log.d("DEBUG", "Reset password successful")
                } else {
                    Log.w("DEBUG", "Reset password fail", task.exception)
                }
            }
    }

    fun signOut(){
        auth.signOut()
    }

    fun postDataToRealTimeDB(data: String){
        myRef.setValue(data)
          .addOnCompleteListener(this){ task ->
               if (task.isSuccessful) {
                   Log.d("DEBUG", "Post data successful")
               } else {
                   Log.w("DEBUG", "Post data fail", task.exception)
               }
           }
    }

    fun readDataFromRealTimeDB() {
        myRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = snapshot.getValue<String>()
                Log.d("DEBUG", "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DEBUG", "Failed to read value.", error.toException())
            }

        })
    }

    fun postDataToFireStore(){
        // Create a new user with a first and last name
        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

        firestore.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("DEBUG", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("DEBUG", "Error adding document", e)
            }
    }

    fun addPostData(data: Post){
        var myRefRoot: DatabaseReference = database.reference
        myRefRoot.child("posts").setValue(data)
          .addOnCompleteListener(this){ task ->
               if (task.isSuccessful) {
                   Log.d("DEBUG", "Post data successful")
               } else {
                   Log.w("DEBUG", "Post data fail", task.exception)
               }
           }
    }
}