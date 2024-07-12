package com.akshat.musicstream.models

data class SongModel(
    val id : String,
    val title : String,
    val subtitle : String,
    val url : String,
    val coverUrl : String,
    val lyrics : String
){
    constructor() :  this("","","","","", "")
}
