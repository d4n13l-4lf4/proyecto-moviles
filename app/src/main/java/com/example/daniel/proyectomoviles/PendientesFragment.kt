package com.example.daniel.proyectomoviles


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.daniel.proyectomoviles.adaptadores.AdaptadorPendientes
import com.example.daniel.proyectomoviles.entidades.Recorrido
import java.util.*
import kotlin.collections.ArrayList

class PendientesFragment : Fragment() {

    lateinit var recyclerRecorrido: RecyclerView
    lateinit var listaRecorridos: ArrayList<Recorrido>
    lateinit var listaPendientes: ArrayList<Recorrido>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(R.layout.fragment_pendientes, container, false)

        listaRecorridos = ArrayList()
        listaPendientes = ArrayList()

        recyclerRecorrido = view.findViewById(R.id.recycler_view_pendientes)
        recyclerRecorrido.layoutManager = LinearLayoutManager(requireContext())

        llenarRecorridos()

        val adaptador = AdaptadorPendientes(listaPendientes,requireContext())
        recyclerRecorrido.adapter = adaptador


        return view
    }

    private fun llenarRecorridos() {


        //Aqui se consultaria todos los recorridos de ese cliente y lo meteria en la listaRecorridos
            //To do


        //Supongamos que el resultado de los recorridos son los objetos de abajo
        listaRecorridos.add(Recorrido(-1, "-155.55896892", "-155.55896892",
                "-0.2033062",
                "-78.49077559999999",
                10.123,
                false,
                "06/08/1996",
                6.123,
                1,
                1,
                0,
                0))

        listaRecorridos.add(Recorrido(-1, "-0.2033062", "-78.49077559999999",
                "-0.2033062",
                "-78.49077559999999",
                6.123,
                false,
                "06/08/1996",
                6.123,
                1,
                1,
                0,
                0))

        listaRecorridos.add(Recorrido(-1, "-0.2033062", "-78.49077559999999",
                "-0.2033062",
                "-78.49077559999999",
                6.123,
                false,
                "06/08/1996",
                6.123,
                1,
                1,
                0,
                0))

        //Log.i("TAMAÑO",listaRecorridos.size.toString())

        //Ahora, para enviar al RV de pendientes, solo se escogen aquellos cuyo estado sea false, por tanto se recorre el arreglo

        listaRecorridos.forEach { recorrido:Recorrido ->

            //Se añaden solo los pendientes
            if(!recorrido.estadoRecorrido){
                listaPendientes.add(recorrido)
            }




        }







    }


}
