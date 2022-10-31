package com.aks.mygroceryadmin.models

import java.io.Serializable

data class ProfileModel(
    val userId: String? = null,
    val userToken: String? = null,
    val username: String? = null,
    val userMailId: String? = null,
    var userAddress: String? = null,
    var userLatLng: String? = null,
    val photoUrl: String? = null,
    var userMobileNo: String? = null,
):Serializable
