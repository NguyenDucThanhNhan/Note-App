package com.ltdd.a7_noteapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
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
import com.ltdd.a7_noteapp.R
import com.ltdd.a7_noteapp.databinding.ActivityMainBinding
import com.ltdd.a7_noteapp.databinding.AddNotesBinding
import com.ltdd.a7_noteapp.databinding.EditNoteBinding
import com.ltdd.a7_noteapp.model.Post
import java.util.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    lateinit var addDialog: AlertDialog
    lateinit var editDialog: AlertDialog

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
            showAddDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mnuSearch){
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
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

                var btnMore: ImageButton = holder.itemView.findViewById(R.id.btn_more)

                btnMore.setOnClickListener {
                    val popupMenu = PopupMenu(it.context, it)
                    popupMenu.gravity = Gravity.END
                    popupMenu.menu.add("Edit").setOnMenuItemClickListener { menuItem ->
                        // Xử lý khi click vào menu Edit
                        editPost(getRef(position).key!!, model)
                        true
                    }
                    popupMenu.menu.add("Delete").setOnMenuItemClickListener { menuItem ->
                        // Xử lý khi click vào menu Delete
                        deletePost(getRef(position).key!!)
                        true
                    }
                    popupMenu.show()
                }

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

    private fun deletePost(postId: String) {
        // Hiển thị hộp thoại xác nhận xóa bài đăng
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { dialog, which ->
                // Xóa bài đăng từ cơ sở dữ liệu Firebase Realtime Database
                myRef.child(postId).removeValue()
                Toast.makeText(this, "Post deleted successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editPost(postId: String, post: Post) {
        val editBuild = AlertDialog.Builder(this, R.style.ThemeCustom)
        val editDialogBinding = EditNoteBinding.inflate(LayoutInflater.from(this))
        editBuild.setView(editDialogBinding.root)

        editDialogBinding.txtEditTitle.setText(post.title)
        editDialogBinding.txtEditContent.setText(post.content)

        editDialogBinding.btnSave.setOnClickListener {
            val newTitle = editDialogBinding.txtEditTitle.text.toString()
            val newContent = editDialogBinding.txtEditContent.text.toString()

            if (newTitle.isNotEmpty() && newContent.isNotEmpty()) {
                updatePost(postId, newTitle, newContent)
                editDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter title and content", Toast.LENGTH_SHORT).show()
            }
        }

        editDialogBinding.btnOut.setOnClickListener {
            editDialog.dismiss()
        }

        editDialog = editBuild.create()
        editDialog.show()
    }


    private fun updatePost(postId: String, newTitle: String, newContent: String) {
        myRef.child(postId).child("title").setValue(newTitle)
        myRef.child(postId).child("content").setValue(newContent)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Post updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update post", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showAddDialog() {
        val build = AlertDialog.Builder(this, R.style.ThemeCustom)
        val dialogBinding = AddNotesBinding.inflate(LayoutInflater.from(this))
        build.setView(dialogBinding.root)
        dialogBinding.btnExit.setOnClickListener {
            addDialog.dismiss()
        }
        dialogBinding.btnSubmit.setOnClickListener {
            val title = dialogBinding.txtAddTitle.text.toString()
            val content = dialogBinding.txtAddContent.text.toString()
            val id = myRef.push().key

            if (id != null) {
                myRef.child(id).setValue(Post(id, title, content, gotRandomColor())).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Add note successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Add note unsuccessful!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            addDialog.dismiss()
        }
        addDialog = build.create()
        addDialog.show()
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