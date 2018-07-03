package com.example.daniel.proyectomoviles.adaptadores

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.daniel.proyectomoviles.R
import com.example.daniel.proyectomoviles.entidades.Recorrido
import com.google.android.gms.maps.model.LatLng

class AdaptadorPendientes (internal var recorridos: ArrayList<Recorrido>, internal var ctx: Context) : RecyclerView.Adapter<AdaptadorPendientes.ViewHolderPendientes>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPendientes {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pendiente,null,false)
        return ViewHolderPendientes(view)
    }

    override fun getItemCount(): Int {
        return recorridos.size
    }

    override fun onBindViewHolder(holder: ViewHolderPendientes, position: Int) {


        //Primero obtenemos el nombre de la dirección destino, segun la latitud y longitud destino
        val localizador = Geocoder(ctx)
        var direccionDestino : List<Address>
        direccionDestino = ArrayList()
        direccionDestino = localizador.getFromLocation(recorridos[position].destinoLatitud.toDouble(), recorridos[position].destinoLongitud.toDouble(),1)


        //Se coloca la dirección en el textView, ejemplo: Foch, Quito
        holder.txtDestinoRecorrido.text = direccionDestino[0].featureName + ", " + direccionDestino[0].locality
        //Se coloca la fecha en la que se solicito el recorrido
        holder.txtFechaRecorrido.text = recorridos[position].fechaRecorrido






    }


    inner class ViewHolderPendientes(itemView: View): RecyclerView.ViewHolder(itemView) {

        internal var txtDestinoRecorrido:TextView
        internal var txtFechaRecorrido:TextView
        internal var btnVerMas:Button

        init {

            txtDestinoRecorrido = itemView.findViewById(R.id.txt_destino_pendiente)
            txtFechaRecorrido = itemView.findViewById(R.id.txt_fecha_pendiente)
            btnVerMas = itemView.findViewById(R.id.btn_detalle_pendiente)

        }


    }


}