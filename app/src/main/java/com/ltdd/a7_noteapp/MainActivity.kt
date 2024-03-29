package com.ltdd.a7_noteapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
import com.google.firebase.database.collection.LLRBNode.Color
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ltdd.a7_noteapp.databinding.ActivityMainBinding
import com.ltdd.a7_noteapp.model.Post
import java.util.Random


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
        myRef = database.getReference("posts")
        firestore = Firebase.firestore

        binding.rvNotes.layoutManager = GridLayoutManager(
            this,
            2,
            RecyclerView.VERTICAL,
            false
        )

        binding.btnAdd.setOnClickListener {
            addNotes()
        }
    }

    fun addNotes(){
        val id = myRef.push().key
        val title = "Test Title"
        val content = "Test content"

        if (id != null) {
            myRef.child(id).setValue(Post(id, title, content, gotRandomColor())).addOnCompleteListener { task ->
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
                holder.layoutNote.setBackgroundColor(android.graphics.Color.parseColor(model.color))
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
        var layoutNote: CardView

        init {
            txtTitle = view.findViewById(R.id.txt_title)
            txtContent = view.findViewById(R.id.txt_content)
            layoutNote = view.findViewById(R.id.layoutNote)
        }
    }

    fun gotRandomColor(): String {
        val colors = ArrayList<String>()
        colors.add("#FFD1DC")
        colors.add("#FFA07A")
        colors.add("#FFB6C1")
        colors.add("#87CEEB")
        colors.add("#98FB98")
        colors.add("#FF69B4")
        colors.add("#ADD8E6")
        colors.add("#F08080")
        colors.add("#90EE90")
        colors.add("#FF6347")
        colors.add("#AFEEEE")
        colors.add("#DB7093")
        colors.add("#FFE4E1")
        colors.add("#FFE4B5")
        colors.add("#F0FFF0")
        colors.add("#BA55D3")
        colors.add("#B0C4DE")
        colors.add("#7B68EE")
        colors.add("#20B2AA")
        colors.add("#9370DB")
        colors.add("#3CB371")
        colors.add("#FF7F50")
        colors.add("#4682B4")
        colors.add("#556B2F")
        colors.add("#D8BFD8")
        colors.add("#5F9EA0")
        colors.add("#DAA520")
        colors.add("#B8860B")
        colors.add("#2E8B57")
        colors.add("#008080")

        val random = Random()
        return colors[random.nextInt(colors.size)]
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