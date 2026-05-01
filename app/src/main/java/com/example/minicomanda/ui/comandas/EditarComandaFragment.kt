package com.example.minicomanda.ui.comandas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.minicomanda.R
import com.example.minicomanda.databinding.FragmentEditarComandaBinding
import com.example.minicomanda.ui.menu.MenuViewModel

class EditarComandaFragment : Fragment() {

    private var _binding: FragmentEditarComandaBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()
    private lateinit var viewModel: EditarComandaViewModel
    private val comandasViewModel: ComandasViewModel by activityViewModels()

    private lateinit var personasPillAdapter: PersonasPillAdapter
    private lateinit var personaOrderAdapter: PersonaOrderAdapter
    private lateinit var menuGridAdapter: MenuGridAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val folio = arguments?.getString("folio") ?: return
        viewModel = EditarComandaViewModel(menuViewModel, folio)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditarComandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostrar información inicial
        binding.tvFolio.text = "Folio: ${viewModel.folio}"
        binding.tvEstado.text = "Estado: ${viewModel.estado.value}"

        // Acciones especiales
        binding.btnCerrarCuenta.setOnClickListener {
            viewModel.cerrarCuenta()
            binding.tvEstado.text = "Estado: CERRADA"
            Toast.makeText(requireContext(), "Cuenta cerrada", Toast.LENGTH_SHORT).show()
        }
        binding.btnMarcarPagado.setOnClickListener {
            viewModel.marcarPagado()
            Toast.makeText(requireContext(), "Marcado como pagado", Toast.LENGTH_SHORT).show()
        }

        // Pills de personas
        personasPillAdapter = PersonasPillAdapter(
            personas = emptyList(),
            selectedIndex = 0,
            onPersonaClick = { index -> viewModel.seleccionarPersona(index) },
            onAddClick = { viewModel.agregarPersona() },
            onDeleteClick = { index -> viewModel.eliminarPersona(index) }
        )
        binding.rvPersonasPills.adapter = personasPillAdapter

        // Grid menú
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
            menuItems = emptyList(),
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
                menuItems = viewModel.menuItems.value ?: emptyList(),
                onIncrement = { personaIndex, menuItem -> viewModel.incrementarItem(personaIndex, menuItem) },
                onDecrement = { personaIndex, menuItem -> viewModel.decrementarItem(personaIndex, menuItem) },
                onDeletePersona = { index -> viewModel.eliminarPersona(index) }
            )
            binding.rvPedidosPersonas.adapter = personaOrderAdapter
        }

        viewModel.totalGeneral.observe(viewLifecycleOwner) { total ->
            binding.tvTotalGeneral.text = "Total: $${"%.2f".format(total)}"
        }

        binding.etNombreCliente.setText(viewModel.nombreCliente.value)
        binding.etNombreCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setNombreCliente(s.toString())
            }
        })

        binding.rgTipoPedido.check(if (viewModel.paraLlevar.value == true) R.id.rb_para_llevar else R.id.rb_comer_aqui)
        binding.rgTipoPedido.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setParaLlevar(checkedId == R.id.rb_para_llevar)
        }

        binding.etObservaciones.setText(viewModel.observaciones.value)
        binding.etObservaciones.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setObservaciones(s.toString())
            }
        })

        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnGuardar.setOnClickListener {
            // TODO: Implementar guardado real cuando la BD esté lista
            Toast.makeText(requireContext(), "Cambios guardados (simulación)", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(folio: String): EditarComandaFragment {
            val fragment = EditarComandaFragment()
            val args = Bundle().apply { putString("folio", folio) }
            fragment.arguments = args
            return fragment
        }
    }
}