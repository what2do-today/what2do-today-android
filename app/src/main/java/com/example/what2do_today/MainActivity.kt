package com.example.what2do_today

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.what2do_today.location.LocationHelper
import com.example.what2do_today.navigation.AppNav

class MainActivity : ComponentActivity() {

    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHelper = LocationHelper(this)

        locationHelper.ensureLocationPermission()

        setContent {
            AppNav(locationHelper = locationHelper)
        }
    }
}