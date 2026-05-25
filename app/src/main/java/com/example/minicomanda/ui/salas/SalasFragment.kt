package com.example.minicomanda.ui.salas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentSalasBinding
import androidx.core.text.HtmlCompat
import androidx.core.widget.NestedScrollView

class SalasFragment : Fragment() {

    private var _binding: FragmentSalasBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSalasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observar cambios en la sala actual
        lobbyViewModel.currentSala.observe(viewLifecycleOwner) { sala ->
            if (sala != null) {
                // Mostrar información de la sala activa
                binding.cardRoomInfo.visibility = View.VISIBLE
                binding.layoutActions.visibility = View.GONE

                binding.tvRoomIdValue.text = "ID: ${sala.id}"
                binding.tvRoomNameValue.text = "Nombre: ${sala.nombre.ifBlank { "Sin nombre" }}"
            } else {
                // Mostrar opciones de crear/unirse
                binding.cardRoomInfo.visibility = View.GONE
                binding.layoutActions.visibility = View.VISIBLE
            }
        }

        // Click en "Crear Sala"
        binding.cardCreate.setOnClickListener {
            val crearFragment = CrearSalaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, crearFragment)
                .addToBackStack(null)
                .commit()
        }

        // Click en "Unirse a Sala"
        binding.cardJoin.setOnClickListener {
            val unirseFragment = UnirseSalaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, unirseFragment)
                .addToBackStack(null)
                .commit()
        }

        // Botón Salir de sala
        binding.btnLeaveRoom.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.TemaDialogoOnboarding) // Usando el tema que ya tienes
                .setTitle("Salir de la sala")
                .setMessage("¿Estás seguro que deseas salir de esta sala?")
                .setPositiveButton("Salir") { dialog, _ ->
                    // Si el usuario confirma, llamamos a la función real del ViewModel
                    lobbyViewModel.salirDeSala()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    // Si se arrepiente, solo cerramos el cuadro sin hacer nada
                    dialog.dismiss()
                }
                .show()
        }

        // Botón Editar sala (funcionalidad futura)
        binding.btnEditRoom.setOnClickListener {
            val editarFragment = EditarSalaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, editarFragment)
                .addToBackStack(null)
                .commit()
        }

        mostrarOnboardingSiEsNecesario()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun mostrarOnboardingSiEsNecesario() {
        val prefs = requireActivity().getSharedPreferences("minicomanda_prefs", android.content.Context.MODE_PRIVATE)

        // Aquí leemos si ya se mostró. (false es el valor por defecto si no existe)
        val yaMostrado = prefs.getBoolean("onboarding_v1_mostrado", false)

        // TODO: Cambia esto a 'false' cuando termines de hacer tus pruebas
        val MODO_TEST = true

        if (!yaMostrado || MODO_TEST) {
            mostrarDialogoOnboarding(prefs)
        }
    }

    private fun mostrarDialogoOnboarding(prefs: android.content.SharedPreferences) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_onboarding, null)
        val scrollView = dialogView.findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollViewOnboarding)
        val tvContenido = dialogView.findViewById<android.widget.TextView>(R.id.tvContenidoOnboarding)
        val btnEntendido = dialogView.findViewById<android.widget.Button>(R.id.btnEntendido)

        // Puedes usar h1, h2, h3, b (negritas), i (cursiva), u (subrayado), br (salto de línea)
        val textoHtml = """
            <h1>¡Bienvenido a MiniComanda!</h1>
            <p>Nos emociona mucho que pruebes esta nueva actualización. Aquí te explicamos algunas cosas clave:</p>
            <br>
            <h2>Nuevas Funcionalidades:</h2>
            <p><b>1. Trabajo Offline:</b> Ahora puedes tomar pedidos incluso si se va el internet.</p>
            <p><b>2. Mejoras visuales:</b> Hemos rediseñado la cocina para que sea más intuitiva.</p>
            <br>
            <p><i>Sigue deslizando hacia abajo para continuar...</i></p>
            <br><br><br><br><br><br><br><br><br><br> <br><br><br><br><br><br><br><br><br><br>
            <h3>¡Todo listo!</h3>
            <p>Presiona el botón de abajo para empezar a trabajar.</p>
        """.trimIndent()

        tvContenido.text = androidx.core.text.HtmlCompat.fromHtml(textoHtml, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.TemaDialogoOnboarding)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        scrollView.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val viewGroup = v.getChildAt(0)
            if (viewGroup != null) {
                // Calculamos si la altura total del contenido ya es visible
                val diferencia = viewGroup.bottom - (v.height + scrollY)
                if (diferencia <= 0) {
                    btnEntendido.isEnabled = true // ¡Desbloqueamos el botón!
                }
            }
        })

        // 4. Qué pasa al presionar el botón
        btnEntendido.setOnClickListener {
            // Guardamos en SharedPreferences que ya lo vio, para que no vuelva a salir
            prefs.edit().putBoolean("onboarding_v1_mostrado", true).apply()
            dialog.dismiss()
        }

        dialog.show()

        // 5. Truco de UX: Si tu texto final es muy corto y no requiere scroll,
        // desbloqueamos el botón de inmediato para que el usuario no se quede atrapado.
        scrollView.post {
            if (scrollView.getChildAt(0).bottom <= scrollView.height) {
                btnEntendido.isEnabled = true
            }
        }
    }
}