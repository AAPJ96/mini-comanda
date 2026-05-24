package com.example.minicomanda

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.minicomanda.databinding.ActivityMainBinding
import com.example.minicomanda.ui.cocina.CocinaFragment
import com.example.minicomanda.ui.comandas.ComandasFragment
import com.example.minicomanda.ui.historial.HistorialFragment
import com.example.minicomanda.ui.menu.MenuFragment
import com.example.minicomanda.ui.salas.LobbyViewModel
import com.example.minicomanda.ui.salas.SalasFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val lobbyViewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del bottom navigation (como estaba)
        binding.bottomNavigation.itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Obtener sala guardada
        val prefs = getSharedPreferences("minicomanda_prefs", MODE_PRIVATE)
        val salaId = prefs.getString("sala_id", null)
        android.util.Log.d("TEST_CREDENCIALES", "Sala guardada: $salaId")

        // Elegir fragmento inicial
        val fragmentoInicial: Fragment
        val tituloInicial: String
        val itemIdInicial: Int
        if (salaId.isNullOrEmpty()) {
            fragmentoInicial = SalasFragment()
            tituloInicial = "SALAS"
            itemIdInicial = R.id.nav_salas
        } else {
            fragmentoInicial = CocinaFragment()
            tituloInicial = "COCINA"
            itemIdInicial = R.id.nav_cocina
        }

        // Cargar fragmento inicial si es primera vez
        if (savedInstanceState == null) {
            loadFragment(fragmentoInicial)
            updateSectionTitle(tituloInicial)
            binding.bottomNavigation.selectedItemId = itemIdInicial
        }

        // Configurar listener de navegación (igual que antes)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_cocina -> CocinaFragment()
                R.id.nav_comandas -> ComandasFragment()
                R.id.nav_historial -> HistorialFragment()
                R.id.nav_menu -> MenuFragment()
                R.id.nav_salas -> SalasFragment()
                else -> null
            }
            fragment?.let {
                loadFragment(it)
                updateSectionTitle(item.title.toString())
                animateIcon(binding.bottomNavigation.findViewById<View>(item.itemId))
                true
            } ?: false
        }

        // Observar la sala actual (para actualizar toolbar)
        lobbyViewModel.currentSala.observe(this) { sala ->
            val roomText = if (sala != null) "${sala.id}" else "---"
            binding.tvRoomId.text = roomText
        }
    }

    /** Centraliza la lógica de cambio de fragmento y título */
    private fun navigateToSection(fragment: Fragment, title: String) {
        loadFragment(fragment)
        updateSectionTitle(title)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun updateSectionTitle(title: String) {
        binding.tvSectionTitle.text = title.uppercase()
    }

    private fun animateIcon(view: View) {
        view.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(150)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
}