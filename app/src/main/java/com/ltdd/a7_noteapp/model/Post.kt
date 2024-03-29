package com.ltdd.a7_noteapp.model

class Post(var id: String = "", var title: String = "", var content: String = "") {
    constructor() : this("", "", "") // constructor không đối số
}
