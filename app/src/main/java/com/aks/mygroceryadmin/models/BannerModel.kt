package com.aks.mygroceryadmin.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BannerModel(
    @Expose
    @SerializedName("imageUrl")
    val imageUrl : String?=null,
    @Expose
    @SerializedName("name")
    val name : String?=null,
    @Expose
    @SerializedName("bannerID")
    val bannerID: String?=null
):Serializable