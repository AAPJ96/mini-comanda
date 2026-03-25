package com.example.minicomanda

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.minicomanda.ui.cocina.CocinaFragment
import com.example.minicomanda.ui.comandas.ComandasFragment
import com.example.minicomanda.ui.historial.HistorialFragment
import com.example.minicomanda.ui.menu.MenuFragment
import com.example.minicomanda.ui.salas.SalasFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottom_navigation)

        /*
        //Ejemplo de implementacion de fragment
        if (savedInstanceState == null) {
            loadFragment(CocinaFragment())
        }
*/
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_cocina -> loadFragment(CocinaFragment())
                R.id.nav_comandas -> loadFragment(ComandasFragment())
                R.id.nav_historial -> loadFragment(HistorialFragment())
                R.id.nav_menu -> loadFragment(MenuFragment())
                R.id.nav_salas -> loadFragment(SalasFragment())
                else -> false
            }
            true
        }

    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        return true
    }
}