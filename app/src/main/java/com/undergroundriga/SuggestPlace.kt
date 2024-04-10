package com.undergroundriga


class SuggestPlace {

    var PlacesId : Int = 0
    var PlaceName : String = ""
    var Description : String = ""
    var Author : String = ""
    var Tag : String = ""
    var PosX : String = ""
    var PosY : String = ""



    constructor(PlaceName : String, Description : String,Author : String, Tag : String,
                PosX : String, PosY : String){
        this.PlaceName = PlaceName
        this.Description = Description
        this.Author = Author
        this.Tag = Tag
        this.PosX = PosX
        this.PosY = PosY

    }

    constructor(){
    }
}