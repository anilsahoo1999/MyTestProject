package com.aks.mygroceryadmin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

object GenerationMethod {

    @SuppressLint("Range")
    fun convertUriToFileName(context : Context, uri: Uri) : String{
        var ret: String? = null
        val scheme: String? = uri.scheme
        if (scheme == "file") {
            ret = uri.lastPathSegment.toString()
        } else if (scheme == "content") {
            val cursor: Cursor? =
                context.contentResolver.query(uri, null, null, null, null)
            if (cursor?.moveToFirst() == true) {
                ret = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return ret.toString()
    }
}