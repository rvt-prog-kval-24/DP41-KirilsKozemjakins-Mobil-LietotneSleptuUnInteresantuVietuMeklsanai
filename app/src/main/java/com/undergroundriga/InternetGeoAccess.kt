package com.undergroundriga

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Button
import android.view.LayoutInflater

fun showAlertDialog(context: Context, inflater: LayoutInflater) {
    val builder = AlertDialog.Builder(context)
    val dialogView = inflater.inflate(R.layout.custom_alert_dialog, null)
    builder.setView(dialogView)

    val positiveButton: Button = dialogView.findViewById(R.id.positiveButton)
    val negativeButton: Button = dialogView.findViewById(R.id.negativeButton)

    val alertDialog = builder.create()

    positiveButton.setOnClickListener {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
        alertDialog.dismiss()
    }

    negativeButton.setOnClickListener {
        alertDialog.dismiss()
    }

    alertDialog.show()
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}