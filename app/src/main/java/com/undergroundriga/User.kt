package com.undergroundriga

class User {

    var id : Int = 0
    var username : String = ""
    var password : String = ""
    var email : String = ""
    var role : String = ""


    constructor(username : String, password : String, email : String, role : String){
        this.username = username
        this.password = password
        this.role = role
        this.email = email

    }

    constructor(){
    }
}