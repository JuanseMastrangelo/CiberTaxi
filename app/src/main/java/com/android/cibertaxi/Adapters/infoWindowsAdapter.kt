package com.android.cibertaxi.Adapters

import com.google.android.gms.maps.model.Marker
import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import com.android.cibertaxi.R
import com.google.android.gms.maps.GoogleMap


class infoWindowsAdapter(mContext: Context) : GoogleMap.InfoWindowAdapter {


    private val mWindow: View

    init {
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.infowindows, null)
    }

    private fun rendowWindowText(marker: Marker, view: View) {

        // Variables default de Google Maps:
        //val title = marker.title
        // val snippet = marker.snippet
        //val tvSnippet = view.findViewById(R.id.snippet) as TextView
        //tvSnippet.text = snippet


        val txtusuario = view.findViewById(R.id.usuario) as TextView
        txtusuario.text = "Nombre: "+marker.title




    }

    override fun getInfoWindow(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }
}