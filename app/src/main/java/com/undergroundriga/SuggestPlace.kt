package com.undergroundriga

class SuggestPlace {

    var PlacesId: Int = 0
    var PlaceName: String = ""
    var Description: String = ""
    var userId: Int = 0 // Changed from Author to userId
    var Tag: String = ""
    var PosX: String = ""
    var PosY: String = ""

    constructor(PlaceName: String, Description: String, userId: Int, Tag: String, PosX: String, PosY: String) {
        this.PlaceName = PlaceName
        this.Description = Description
        this.userId = userId
        this.Tag = Tag
        this.PosX = PosX
        this.PosY = PosY
    }

    constructor() {}
}
