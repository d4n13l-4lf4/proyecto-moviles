package com.example.daniel.proyectomoviles.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.daniel.proyectomoviles.PanelActivity

import com.example.daniel.proyectomoviles.R
import com.example.daniel.proyectomoviles.baseDeDatos.DBHandler
import com.example.daniel.proyectomoviles.entidades.*
import com.example.daniel.proyectomoviles.http.HttpRequest
import com.example.daniel.proyectomoviles.parser.JsonParser
import com.example.daniel.proyectomoviles.utilities.Hash
import com.example.daniel.proyectomoviles.utilities.ImageFileHandler
import kotlinx.android.synthetic.main.fragment_auth_fragment.*
import kotlinx.android.synthetic.main.fragment_login_fragment.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AuthFragment : Fragment() {

    var passwordHiding = true
    var idVisibilityOffRes: Int = 0
    var idVisibilityOnRes: Int = 0
    var imagePath = ""
    lateinit var imageBitmap: Bitmap
    var username: String = ""
    var password: String = ""
    val jsonParser = JsonParser()
    lateinit var imm: InputMethodManager

    companion object {
        val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments!!.getString("USERNAME")
        idVisibilityOffRes = resources.getIdentifier("@drawable/ic_baseline_visibility_off_24px", "drawable", activity!!.packageName)
        idVisibilityOnRes = resources.getIdentifier("@drawable/ic_baseline_visibility_24px", "drawable", activity!!.packageName)
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar_frag_auth.visibility = View.GONE

        imgBtn_password_visibility.setOnClickListener { v: View? ->
            if(passwordHiding){
                imgBtn_password_visibility.setImageResource(idVisibilityOnRes)
                editText_frag_auth_password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordHiding = false
            }
            else {
                imgBtn_password_visibility.setImageResource(idVisibilityOffRes)
                editText_frag_auth_password.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordHiding = true
            }
        }

        imgBtn_frag_auth_next.setOnClickListener { v: View? ->
            imm.hideSoftInputFromWindow(editText_frag_auth_password.windowToken, 0)
            progressBar_frag_auth.visibility = View.VISIBLE
            password = Hash.stringHash("SHA-512", editText_frag_auth_password.text.toString())

            HttpRequest.authentication(username, password, callback = { error, datos ->
                progressBar_frag_auth.visibility = View.GONE
                if(error){
                    textView_frag_auth_feed.text = resources.getString(R.string.sign_in_auth_error_feedback)
                }else{
                    textView_frag_auth_feed.text = ""
                    Log.i("AUTH_SERV_RES", "EMPIEZA ASYNC")
                    async(UI) {
                        val cliente: Deferred<Cliente?> = bg {
                            jsonParser.jsonToCliente(datos)
                        }

                        guardarDatosEnBD(cliente.await() as Cliente)
                    }

                }
            })
        }

        btn_facial_recognition.setOnClickListener{
            tomarFoto(this.activity!!.baseContext)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            progressBar_frag_auth.visibility = View.VISIBLE
            async(UI){
                val imageRotated: Deferred<String> = bg{
                    ImageFileHandler.base64FromFileRotation(File(imagePath))
                }
                    realizarAutenticacion(imageRotated.await())
            }
        }
    }

    fun tomarFoto(context: Context){
        val imageFile = createImageFile("JPEG_", Environment.DIRECTORY_PICTURES, ".jpg")
        tomarFotoIntent(imageFile, context)
    }

    fun createImageFile(prefix: String, directory: String, extension: String): File {
        val timestamp = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())
        val filename = prefix + timestamp + "_"
        val storageDir = activity!!.getExternalFilesDir(directory)
        return File.createTempFile(filename, extension, storageDir)
    }

    fun tomarFotoIntent(file: File, context: Context){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imagePath = file.absolutePath
        val photoUri: Uri = FileProvider.getUriForFile(
                context,
                "com.example.daniel.proyectomoviles.fileprovider",
                file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        if(intent.resolveActivity(activity!!.packageManager) != null){
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    fun guardarDatosEnBD(cliente: Cliente){
        /*Foto cliente conductor tarjeta Recorrido*/
        DBHandler.getInstance(activity!!.baseContext)!!.insertar(cliente.foto as Foto)
        DBHandler.getInstance(activity!!.baseContext)!!.insertar(cliente)
        cliente.tarjetasDeCredito!!.forEach { tarjetaCredito: TarjetaCredito ->
            DBHandler.getInstance(activity!!.baseContext)!!.insertar(tarjetaCredito)
            tarjetaCredito.recorridos!!.forEach { recorrido: Recorrido ->
                DBHandler.getInstance(activity!!.baseContext)!!.insertar(recorrido.conductor as Conductor)
                DBHandler.getInstance(activity!!.baseContext)!!.insertar(recorrido)
            }
        }

        Toast.makeText(activity!!.baseContext, String.format(activity!!.resources.getString(R.string.sign_in_auth_logged), cliente.nombre +" "+ cliente.apellido), Toast.LENGTH_LONG).show()
        ImageFileHandler.writeFile( createImageFile("JPEG_RECV_", Environment.DIRECTORY_PICTURES, ".jpg"), Base64.decode(cliente.foto.datos, Base64.URL_SAFE or Base64.NO_WRAP))
        Log.i("AUTH_SERV_RES", cliente.jwt)
        Log.i("AUTH_SERV_RES", cliente.toString())
        irActividadPanel()
    }

    fun realizarAutenticacion(imageBase64: String){
        textView_frag_auth_feed.text = "AUTENTICANDO"
        HttpRequest.authentication(username, foto = imageBase64, callback = { error, datos ->
            progressBar_frag_auth.visibility = View.GONE
            if(error){
                textView_frag_auth_feed.text = resources.getString(R.string.sign_in_auth_error_feedback)
            }else{
                textView_frag_auth_feed.text = "LLEGARON LOS DATOS"
                async(UI) {
                    val cliente: Deferred<Cliente?> = bg {
                        jsonParser.jsonToCliente(datos)
                    }
                    guardarDatosEnBD(cliente.await() as Cliente)
                }
            }
        })
    }

    fun irActividadPanel(){
        val intent = Intent(this.context, PanelActivity::class.java)
        startActivity(intent)
    }

}
