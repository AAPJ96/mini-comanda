package com.example.minicomanda.ui.comandas

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentEditarComandaBinding

class EditarComandaFragment : Fragment() {

    private var _binding: FragmentEditarComandaBinding? = null
    private val binding get() = _binding!!

    // El ViewModel se crea usando una factory que recibe el ID de la comanda
    private lateinit var viewModel: EditarComandaViewModel
    private val comandasViewModel: ComandasViewModel by activityViewModels()

    private lateinit var personasPillAdapter: PersonasPillAdapter
    private lateinit var personaOrderAdapter: PersonaOrderAdapter
    private lateinit var menuGridAdapter: MenuGridAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtenemos el ID de la comanda de los argumentos
        val comandaId = arguments?.getString("comanda_id") ?: return

        // Inicializamos el ViewModel con la factory adecuada
        viewModel = ViewModelProvider(
            this,
            EditarComandaViewModel.Factory(requireActivity().application, comandaId)
        ).get(EditarComandaViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditarComandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar información inicial
        binding.tvFolio.text = viewModel.folio?.let { "Folio: $it" } ?: "Sin folio"
        binding.tvEstado.text = "Estado: ${viewModel.estado.value}"

        // Botón marcar pagado
        // Botón marcar pagado con Dialog de confirmación y autoguardado
        binding.btnMarcarPagado.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Comanda pagada")
                .setMessage("¿Estás seguro de marcar esta orden como PAGADA?")
                .setPositiveButton("Sí") { _, _ ->
                    // 1. Actualizamos el estado internamente en el ViewModel
                    viewModel.marcarPagado()

                    // 2. Disparamos la misma lógica del botón "Guardar"
                    val (comanda, detalles) = viewModel.construirComandaActualizada()
                    comandasViewModel.actualizarComanda(comanda, detalles)

                    Toast.makeText(requireContext(), "Comanda cobrada y guardada", Toast.LENGTH_SHORT).show()

                    // 3. Salimos a la lista
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Píldoras de personas
        personasPillAdapter = PersonasPillAdapter(
            personas = emptyList(),
            selectedIndex = 0,
            onPersonaClick = { index -> viewModel.seleccionarPersona(index) },
            onAddClick = { viewModel.agregarPersona() },
            onDeleteClick = { index -> viewModel.eliminarPersona(index) }
        )
        binding.rvPersonasPills.adapter = personasPillAdapter

        // Grid del menú
        gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuGrid.layoutManager = gridLayoutManager
        menuGridAdapter = MenuGridAdapter(emptyList()) { menuItem ->
            viewModel.agregarItemAMenu(menuItem)
        }
        binding.rvMenuGrid.adapter = menuGridAdapter

        // Seekbar columnas
        binding.seekbarColumnas.progress = 1
        binding.tvNumColumnas.text = "2"
        binding.seekbarColumnas.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val columnas = progress + 1
                binding.tvNumColumnas.text = columnas.toString()
                gridLayoutManager.spanCount = columnas
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Pedidos por persona
        personaOrderAdapter = PersonaOrderAdapter(
            personas = emptyList(),
            pedidosPorPersona = emptyMap(),
            menuItems = emptyList(),   // corregido: menuItems en lugar de itemMenus
            onIncrement = { personaIndex, menuItem -> viewModel.incrementarItem(personaIndex, menuItem) },
            onDecrement = { personaIndex, menuItem -> viewModel.decrementarItem(personaIndex, menuItem) },
            onDeletePersona = { index -> viewModel.eliminarPersona(index) }
        )
        binding.rvPedidosPersonas.adapter = personaOrderAdapter

        // Observadores
        viewModel.personas.observe(viewLifecycleOwner) { personas ->
            personasPillAdapter = PersonasPillAdapter(
                personas = personas,
                selectedIndex = viewModel.selectedPersonaIndex.value ?: 0,
                onPersonaClick = { index -> viewModel.seleccionarPersona(index) },
                onAddClick = { viewModel.agregarPersona() },
                onDeleteClick = { index -> viewModel.eliminarPersona(index) }
            )
            binding.rvPersonasPills.adapter = personasPillAdapter
        }

        viewModel.selectedPersonaIndex.observe(viewLifecycleOwner) { selectedIndex ->
            personasPillAdapter = PersonasPillAdapter(
                personas = viewModel.personas.value ?: emptyList(),
                selectedIndex = selectedIndex,
                onPersonaClick = { index -> viewModel.seleccionarPersona(index) },
                onAddClick = { viewModel.agregarPersona() },
                onDeleteClick = { index -> viewModel.eliminarPersona(index) }
            )
            binding.rvPersonasPills.adapter = personasPillAdapter
            binding.rvPersonasPills.smoothScrollToPosition(selectedIndex)
        }

        // Observar el menú (se llama menuItems)
        viewModel.menuItems.observe(viewLifecycleOwner) { items ->
            menuGridAdapter = MenuGridAdapter(items) { menuItem ->
                viewModel.agregarItemAMenu(menuItem)
            }
            binding.rvMenuGrid.adapter = menuGridAdapter
        }

        viewModel.pedidosPorPersonaUi.observe(viewLifecycleOwner) { pedidosMap ->
            personaOrderAdapter = PersonaOrderAdapter(
                personas = viewModel.personas.value ?: emptyList(),
                pedidosPorPersona = pedidosMap,
                menuItems = viewModel.menuItems.value ?: emptyList(),  // corregido
                onIncrement = { personaIndex, menuItem -> viewModel.incrementarItem(personaIndex, menuItem) },
                onDecrement = { personaIndex, menuItem -> viewModel.decrementarItem(personaIndex, menuItem) },
                onDeletePersona = { index -> viewModel.eliminarPersona(index) }
            )
            binding.rvPedidosPersonas.adapter = personaOrderAdapter
        }

        viewModel.totalGeneral.observe(viewLifecycleOwner) { total ->
            binding.tvTotalGeneral.text = "Total: $${"%.2f".format(total)}"
        }

        // Nombre cliente
        viewModel.nombreCliente.observe(viewLifecycleOwner) { nombre ->
            // Verificamos que sea diferente para evitar un loop infinito con el TextWatcher
            if (binding.etNombreCliente.text.toString() != nombre) {
                binding.etNombreCliente.setText(nombre)
            }
        }

        binding.etNombreCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setNombreCliente(s.toString())
            }
        })

        // Tipo de pedido
        viewModel.paraLlevar.observe(viewLifecycleOwner) { paraLlevar ->
            val expectedId = if (paraLlevar == true) R.id.rb_para_llevar else R.id.rb_comer_aqui
            if (binding.rgTipoPedido.checkedRadioButtonId != expectedId) {
                binding.rgTipoPedido.check(expectedId)
            }
        }

        binding.rgTipoPedido.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setParaLlevar(checkedId == R.id.rb_para_llevar)
        }

        // Observaciones
        viewModel.observaciones.observe(viewLifecycleOwner) { notas ->
            // Verificamos que sea diferente para evitar loops con el TextWatcher
            if (binding.etObservaciones.text.toString() != notas) {
                binding.etObservaciones.setText(notas)
            }
        }

        binding.etObservaciones.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setObservaciones(s.toString())
            }
        })

        viewModel.mensaje.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                // Después de cancelar, volver atrás
                parentFragmentManager.popBackStack()
            }
        }

        binding.btnCancelarComanda.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cancelar comanda")
                .setMessage("¿Estás seguro de que deseas cancelar esta comanda? Se eliminará de la lista activa.")
                .setPositiveButton("Sí, cancelar") { _, _ ->
                    viewModel.cancelarComanda()
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.btnCerrar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnGuardar.setOnClickListener {
            val (comanda, detalles) = viewModel.construirComandaActualizada()
            comandasViewModel.actualizarComanda(comanda, detalles)
            Toast.makeText(requireContext(), "Comanda actualizada", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(comandaId: String): EditarComandaFragment {
            val fragment = EditarComandaFragment()
            val args = Bundle().apply { putString("comanda_id", comandaId) }
            fragment.arguments = args
            return fragment
        }
    }
}