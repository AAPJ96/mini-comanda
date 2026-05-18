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

        // Solución para la "píldora" de selección (API 28 compatible)
        binding.bottomNavigation.itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)

        // Configuración de Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Fragment inicial
        if (savedInstanceState == null) {
            navigateToSection(CocinaFragment(), "Cocina")
        }

        // Listener de navegación
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
                navigateToSection(it, item.title.toString())

                // Animación: Buscamos la vista del ítem seleccionado
                val itemView = binding.bottomNavigation.findViewById<View>(item.itemId)
                animateIcon(itemView)
                true
            } ?: false
        }

        // Observar la sala actual
        lobbyViewModel.currentSala.observe(this) { sala ->
            val roomText = if (sala != null) "Sala: ${sala.id}" else "Sala: ---"
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