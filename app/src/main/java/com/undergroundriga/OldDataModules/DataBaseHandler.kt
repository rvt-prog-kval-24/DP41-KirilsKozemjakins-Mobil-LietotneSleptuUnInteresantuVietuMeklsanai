package com.undergroundriga

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import android.database.Cursor

const val DATABASE_NAME = "HiddenRiga"
const val TABLE_NAME = "Users"
const val COL_USERNAME = "username"
const val COL_PASSWORD = "password"
const val COL_ID = "id"
const val COL_ROLE = "role"
const val COL_EMAIL = "email"


const val TABLE_NAME_MAPS = "MapsPlaces"
const val COL_PLACESID = "PlacesId"
const val COL_PLACENAME = "PlaceName"
const val COL_DESCRIPTION = "Description"
const val COL_POSX = "PosX"
const val COL_POSY = "PosY"
const val COL_TAG = "Tag"

const val TABLE_NAME_SUGGESTIONS = "MapsPlacesSuggestions"
const val COL_PLACESID_SUGGESTION= "PlacesId"
const val COL_PLACENAME_SUGGESTION = "PlaceName"
const val COL_DESCRIPTION_SUGGESTION = "Description"
const val COL_AUTHOR_ID_SUGGESTION = "AuthorID"
const val COL_POSX_SUGGESTION = "PosX"
const val COL_POSY_SUGGESTION = "PosY"
const val COL_TAG_SUGGESTION = "Tag"

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {


    override fun onCreate(db: SQLiteDatabase?) {
        val createTableUsers = "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_USERNAME VARCHAR(12), " +
                "$COL_PASSWORD VARCHAR(15), " +
                "$COL_EMAIL VARCHAR(25), " +
                "$COL_ROLE VARCHAR(1));"

        val createTablePlaces = "CREATE TABLE $TABLE_NAME_MAPS (" +
                "$COL_PLACESID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_PLACENAME TEXT, " +
                "$COL_DESCRIPTION TEXT, " +
                "$COL_TAG TEXT, " +
                "$COL_POSX TEXT, " +
                "$COL_POSY TEXT);"

        val createTablePlacesSuggestions = "CREATE TABLE $TABLE_NAME_SUGGESTIONS (" +
                "$COL_PLACESID_SUGGESTION INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_PLACENAME_SUGGESTION TEXT, " +
                "$COL_DESCRIPTION_SUGGESTION TEXT, " +
                "$COL_AUTHOR_ID_SUGGESTION INTEGER, " +
                "$COL_TAG_SUGGESTION TEXT, " +
                "$COL_POSX_SUGGESTION TEXT, " +
                "$COL_POSY_SUGGESTION TEXT, " +
                "FOREIGN KEY($COL_AUTHOR_ID_SUGGESTION) REFERENCES $TABLE_NAME($COL_ID));"

        db?.execSQL(createTableUsers)
        db?.execSQL(createTablePlaces)
        db?.execSQL(createTablePlacesSuggestions)
    }


    fun isUsernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COL_USERNAME = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COL_EMAIL = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    fun insertData(user: User){

        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COL_USERNAME,user.username)
        cv.put(COL_PASSWORD,user.password)
        cv.put(COL_EMAIL,user.email)
        cv.put(COL_ROLE,user.role)

        var result = db.insert(TABLE_NAME,null, cv)

        if(result == -1.toLong()){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()}


    }

    fun insertDataPlaces(places: Places){

        val db = this.writableDatabase
        var cvPlaces = ContentValues()
        cvPlaces.put(COL_PLACENAME,places.PlaceName)
        cvPlaces.put(COL_DESCRIPTION,places.Description)
        cvPlaces.put(COL_TAG,places.Tag)
        cvPlaces.put(COL_POSX,places.PosX)
        cvPlaces.put(COL_POSY,places.PosY)

        var result = db.insert(TABLE_NAME_MAPS,null, cvPlaces)

        if(result == -1.toLong()){
            Toast.makeText(context, "Failed" + places.PlaceName+places.Description+
                    places.Tag+places.PosX+places.PosY, Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()}


    }

    fun readDataMapsPlaces() : MutableList<Places>{
        var list : MutableList<Places> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME_MAPS
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do {
                var places = Places()
                places.PlacesId = result.getString(result.getColumnIndex(COL_PLACESID)).toInt()
                places.PlaceName = result.getString(result.getColumnIndex(COL_PLACENAME))
                places.Description = result.getString(result.getColumnIndex(COL_DESCRIPTION))
                places.Tag = result.getString(result.getColumnIndex(COL_TAG))
                places.PosX = result.getString(result.getColumnIndex(COL_POSX))
                places.PosY = result.getString(result.getColumnIndex(COL_POSY))
                list.add(places)
            }while (result.moveToNext())
        }

        result.close()
        db.close()
        return list

    }

    fun getAllPlaceNames(): List<String> {
        val placeNames = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT $COL_PLACENAME FROM $TABLE_NAME_MAPS"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val placeName = result.getString(result.getColumnIndex(COL_PLACENAME))
                placeNames.add(placeName)
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return placeNames
    }

    fun getAllPlaceIds(): List<Int> {
        val placeIds = mutableListOf<Int>()
        val db = this.readableDatabase
        val query = "SELECT $COL_PLACESID FROM $TABLE_NAME_MAPS"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val placeId = result.getInt(result.getColumnIndex(COL_PLACESID))
                placeIds.add(placeId)
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return placeIds
    }

    fun getAllPlaceNamesAndIds(): List<Pair<Int, String>> {
        val placeNamesAndIds = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val query = "SELECT $COL_PLACESID, $COL_PLACENAME FROM $TABLE_NAME_MAPS"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val placeId = result.getInt(result.getColumnIndex(COL_PLACESID))
                val placeName = result.getString(result.getColumnIndex(COL_PLACENAME))
                val pair = Pair(placeId, placeName)
                placeNamesAndIds.add(pair)
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return placeNamesAndIds
    }



    fun readDataUsers() : MutableList<User>{
        var list : MutableList<User> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()){
            do {
                var user = User()
                user.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                user.username = result.getString(result.getColumnIndex(COL_USERNAME))
                user.password = result.getString(result.getColumnIndex(COL_PASSWORD))
                user.email = result.getString(result.getColumnIndex(COL_EMAIL))
                user.role = result.getString(result.getColumnIndex(COL_ROLE))
                list.add(user)
            }while (result.moveToNext())
        }

        result.close()
        db.close()
        return list

    }

    fun deleteUserData(id: Int) {
        val db = this.writableDatabase
        val whereClause = "$COL_ID = ?"
        val whereArgs = arrayOf(id.toString())
        val result = db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()

        if (result != -1) {
            Toast.makeText(context, "User with ID $id deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to delete user with ID $id", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteMapsData(id: Int) {
        val db = this.writableDatabase
        val whereClause = "$COL_PLACESID = ?"
        val whereArgs = arrayOf(id.toString())
        val result = db.delete(TABLE_NAME_MAPS, whereClause, whereArgs)
        db.close()

        if (result != -1) {
            Toast.makeText(context, "Place with ID $id deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to delete user with ID $id", Toast.LENGTH_SHORT).show()
        }
    }


    fun updateUserData(id: Int, newUsername: String, newPassword: String, newEmail: String ,newRole: String) {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COL_USERNAME, newUsername)
        cv.put(COL_PASSWORD, newPassword)
        cv.put(COL_EMAIL, newEmail)
        cv.put(COL_ROLE, newRole)

        val whereClause = "$COL_ID = ?"
        val whereArgs = arrayOf(id.toString())

        val result = db.update(TABLE_NAME, cv, whereClause, whereArgs)
        db.close()

        if (result != -1) {
            Toast.makeText(context, "User with ID $id updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to update user with ID $id", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDataPlaces(id: Int, newPlaceName: String, newDescription: String, newTag: String, newPosX: String, newPosY: String) {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COL_PLACENAME, newPlaceName)
        cv.put(COL_DESCRIPTION, newDescription)
        cv.put(COL_TAG, newTag)
        cv.put(COL_POSX, newPosX)
        cv.put(COL_POSY, newPosY)

        val whereClause = "$COL_PLACESID = ?"
        val whereArgs = arrayOf(id.toString())

        val result = db.update(TABLE_NAME_MAPS, cv, whereClause, whereArgs)
        db.close()

        if (result != -1) {
            Toast.makeText(context, "Place with ID $id updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to update place with ID $id", Toast.LENGTH_SHORT).show()
        }
    }

    fun insertDataPlacesSuggestions(suggestPlace: SuggestPlace) {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COL_PLACENAME_SUGGESTION, suggestPlace.PlaceName)
        cv.put(COL_DESCRIPTION_SUGGESTION, suggestPlace.Description)
        cv.put(COL_AUTHOR_ID_SUGGESTION, suggestPlace.userId) // Changed from AuthorID to userId
        cv.put(COL_TAG_SUGGESTION, suggestPlace.Tag)
        cv.put(COL_POSX_SUGGESTION, suggestPlace.PosX)
        cv.put(COL_POSY_SUGGESTION, suggestPlace.PosY)

        val result = db.insert(TABLE_NAME_SUGGESTIONS, null, cv)

        if (result == -1L) {
            Toast.makeText(context, "Failed to insert suggestion", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Suggestion inserted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun getMySuggestions(userId: Int): List<SuggestPlace> {
        val suggestions = mutableListOf<SuggestPlace>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME_SUGGESTIONS WHERE $COL_AUTHOR_ID_SUGGESTION = ?"
        val selectionArgs = arrayOf(userId.toString())
        val result = db.rawQuery(query, selectionArgs)

        if (result.moveToFirst()) {
            do {
                val placeId = result.getInt(result.getColumnIndex(COL_PLACESID_SUGGESTION))
                val placeName = result.getString(result.getColumnIndex(COL_PLACENAME_SUGGESTION))
                val description = result.getString(result.getColumnIndex(COL_DESCRIPTION_SUGGESTION))
                val tag = result.getString(result.getColumnIndex(COL_TAG_SUGGESTION))
                val posX = result.getString(result.getColumnIndex(COL_POSX_SUGGESTION))
                val posY = result.getString(result.getColumnIndex(COL_POSY_SUGGESTION))
                val suggestion = SuggestPlace(placeName, description, userId, tag, posX, posY)
                suggestions.add(suggestion)
            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return suggestions
    }






    /*56.9514998970168,
      24.10635958457534
    * Zviedru vārti
    17. gadsimta vārti saglabājies viduslaiku pilsētas mūra posms.
    *
    sight*/
}