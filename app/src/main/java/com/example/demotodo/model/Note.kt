package com.example.demotodo.model

import java.io.Serializable

class Note(
    val id: String,
    var tile: String,
    var content: String,
    var isNew: Boolean = true
) : Serializable {

}
