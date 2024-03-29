package com.ltdd.a7_noteapp.model

class Post(var id: String = "", var title: String = "", var content: String = "", var color: String = "") {
    constructor() : this("", "", "", "") // constructor không đối số
}
