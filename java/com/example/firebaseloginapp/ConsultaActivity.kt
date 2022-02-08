package com.example.firebaseloginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import com.example.firebaseloginapp.databinding.ActivityConsultaBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class ConsultaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultaBinding
    private lateinit var adapterPaises: ArrayAdapter<String>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var paisesList = cargarPaises()

        binding.spPais.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                val pais = paisesList[pos]
                when (pais) {
                    "ESPAÑA" -> cargarCiudades("ES")
                    "ALEMANIA" -> cargarCiudades("DE")
                    "FRANCIA" -> cargarCiudades("FR")
                    "ITALIA" -> cargarCiudades("IT")
                    "INGLATERRA" -> cargarCiudades("UK")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not implemented")
            }
        }
    }

    private fun cargarPaises(): ArrayList<String> {
        var paisesList = ArrayList<String>()

        db.collection("paises").get().addOnSuccessListener {
            paises -> for (pais in paises) {
                paisesList.add(pais.get("nombre").toString())
            }
            adapterPaises = ArrayAdapter(this, android.R.layout.simple_spinner_item, paisesList)
            adapterPaises.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spPais.adapter = adapterPaises
        }
        return paisesList
    }

    private fun cargarCiudades(cod_pais: String) {
        var ciudadesList = ArrayList<String>()
        db.collection("ciudades").get().addOnSuccessListener {
            ciudades -> for (ciudad in ciudades) {
                if (ciudad.get("cod_pais").toString() == cod_pais) {
                    ciudadesList.add(ciudad.get("nombre").toString())
                }
            }
            val adapterCiudades = ArrayAdapter(this, android.R.layout.simple_spinner_item, ciudadesList)
            adapterCiudades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spCiudad.adapter = adapterCiudades
        }

        binding.spCiudad.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val ciudad = ciudadesList[pos]
                var datos = HashMap<String, String>()
                db.collection("ciudades")
                    .whereEqualTo("nombre", ciudad).get().addOnSuccessListener {
                        ciudades -> for (ciudad in ciudades) {
                            datos.put("ciudad",ciudad.get("nombre").toString())
                            datos.put("poblacion_ciudad", ciudad.get("poblacion").toString())

                            db.collection("paises").get().addOnSuccessListener { paises ->
                                for (pais in paises) {
                                    if (ciudad.get("cod_pais").toString() == pais.id) {
                                        datos.put("pais", pais.get("nombre").toString())
                                        datos.put(
                                            "poblacion_pais",
                                            pais.get("poblacion").toString()
                                        )
                                    }
                                }
                                var monumentosCiudad = ArrayList<String>()
                                db.collection("monumentos")
                                    .whereEqualTo("cod_poblacion", ciudad.id).get().addOnSuccessListener { monumentos ->
                                        for (monumento in monumentos) {
                                            monumentosCiudad.add(monumento.get("nombre").toString())
                                        }
                                        datos.put("monumentos",monumentosCiudad.toString())
                                        mostrarDatos(datos)
                                    }
                            }
                        }
                    }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not implemented")
            }
        }
    }
    private fun mostrarDatos(data: HashMap<String,String>) {
        val poblacionPais = data.get("poblacion_pais")!!.toDouble()
        val poblacionCiudad = data.get("poblacion_ciudad")!!.toDouble()

        binding.txtPaisSel.text = "Pais: ${data.get("pais")}"
        binding.txtCiudadSel.text = "Ciudad: ${data.get("ciudad")}"
        binding.txtPoblacionPais.text = "Población del pais: ${data.get("poblacion_pais")}"

        val porcentaje = ((poblacionCiudad/poblacionPais)*100)
        val strPorcentaje = String.format("%.1f",porcentaje)

        binding.txtPoblacionCiudad.text = "Habitantes: ${data.get("poblacion_ciudad")} ($strPorcentaje% de la población del pais)"
        binding.txtMonumentos.text = "Monumentos: ${data.get("monumentos")}"
    }
}