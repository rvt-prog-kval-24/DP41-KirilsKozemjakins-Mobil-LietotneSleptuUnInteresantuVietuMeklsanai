package com.undergroundriga


class Places {

    var PlacesId : Int = 0
    var PlaceName : String = ""
    var Description : String = ""
    var Tag : String = ""
    var PosX : String = ""
    var PosY : String = ""



    constructor(PlaceName : String, Description : String, Tag : String,
                PosX : String, PosY : String){
        this.PlaceName = PlaceName
        this.Description = Description
        this.Tag = Tag
        this.PosX = PosX
        this.PosY = PosY

    }

    constructor(){
    }
}