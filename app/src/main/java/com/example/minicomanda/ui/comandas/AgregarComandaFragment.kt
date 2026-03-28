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
import com.example.minicomanda.databinding.FragmentAgregarComandaBinding
import com.example.minicomanda.ui.menu.MenuViewModel
import com.example.minicomanda.R

class AgregarComandaFragment : Fragment() {

    private var _binding: FragmentAgregarComandaBinding? = null
    private val binding get() = _binding!!

    private val menuViewModel: MenuViewModel by activityViewModels()
    private val agregarComandaViewModel: AgregarComandaViewModel by lazy {
        AgregarComandaViewModel(menuViewModel)
    }

    private lateinit var personasPillAdapter: PersonasPillAdapter
    private lateinit var personaOrderAdapter: PersonaOrderAdapter
    private lateinit var menuGridAdapter: MenuGridAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAgregarComandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView de píldoras de personas
        personasPillAdapter = PersonasPillAdapter(
            personas = emptyList(),
            selectedIndex = 0,
            onPersonaClick = { index -> agregarComandaViewModel.seleccionarPersona(index) },
            onAddClick = { agregarComandaViewModel.agregarPersona() },
            onDeleteClick = { index -> agregarComandaViewModel.eliminarPersona(index) }
        )
        binding.rvPersonasPills.adapter = personasPillAdapter

        // Configurar grid del menú
        gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMenuGrid.layoutManager = gridLayoutManager
        menuGridAdapter = MenuGridAdapter(emptyList()) { menuItem ->
            agregarComandaViewModel.agregarItemAMenu(menuItem)
        }
        binding.rvMenuGrid.adapter = menuGridAdapter

        // Configurar seekbar para columnas
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

        // Configurar RecyclerView de pedidos por persona
        personaOrderAdapter = PersonaOrderAdapter(
            personas = emptyList(),
            pedidosPorPersona = emptyMap(),
            menuItems = emptyList(),
            onIncrement = { personaIndex, menuItem ->
                agregarComandaViewModel.incrementarItem(personaIndex, menuItem)
            },
            onDecrement = { personaIndex, menuItem ->
                agregarComandaViewModel.decrementarItem(personaIndex, menuItem)
            },
            onDeletePersona = { index ->
                agregarComandaViewModel.eliminarPersona(index)
            }
        )
        binding.rvPedidosPersonas.adapter = personaOrderAdapter

        // Observar datos del ViewModel
        agregarComandaViewModel.personas.observe(viewLifecycleOwner) { personas ->
            personasPillAdapter = PersonasPillAdapter(
                personas = personas,
                selectedIndex = agregarComandaViewModel.selectedPersonaIndex.value ?: 0,
                onPersonaClick = { index -> agregarComandaViewModel.seleccionarPersona(index) },
                onAddClick = { agregarComandaViewModel.agregarPersona() },
                onDeleteClick = { index -> agregarComandaViewModel.eliminarPersona(index) }
            )
            binding.rvPersonasPills.adapter = personasPillAdapter
            personasPillAdapter.notifyDataSetChanged()
        }

        agregarComandaViewModel.selectedPersonaIndex.observe(viewLifecycleOwner) { selectedIndex ->
            // Actualizar adaptador de píldoras para reflejar selección
            personasPillAdapter = PersonasPillAdapter(
                personas = agregarComandaViewModel.personas.value ?: emptyList(),
                selectedIndex = selectedIndex,
                onPersonaClick = { index -> agregarComandaViewModel.seleccionarPersona(index) },
                onAddClick = { agregarComandaViewModel.agregarPersona() },
                onDeleteClick = { index -> agregarComandaViewModel.eliminarPersona(index) }
            )
            binding.rvPersonasPills.adapter = personasPillAdapter
            personasPillAdapter.notifyDataSetChanged()

            // Auto-scroll para mostrar la píldora seleccionada
            binding.rvPersonasPills.smoothScrollToPosition(selectedIndex)
        }

        agregarComandaViewModel.menuItems.observe(viewLifecycleOwner) { items ->
            menuGridAdapter = MenuGridAdapter(items) { menuItem ->
                agregarComandaViewModel.agregarItemAMenu(menuItem)
            }
            binding.rvMenuGrid.adapter = menuGridAdapter
            menuGridAdapter.notifyDataSetChanged()
        }

        agregarComandaViewModel.pedidosPorPersonaUi.observe(viewLifecycleOwner) { pedidosMap ->
            personaOrderAdapter = PersonaOrderAdapter(
                personas = agregarComandaViewModel.personas.value ?: emptyList(),
                pedidosPorPersona = pedidosMap,
                menuItems = agregarComandaViewModel.menuItems.value ?: emptyList(),
                onIncrement = { personaIndex, menuItem ->
                    agregarComandaViewModel.incrementarItem(personaIndex, menuItem)
                },
                onDecrement = { personaIndex, menuItem ->
                    agregarComandaViewModel.decrementarItem(personaIndex, menuItem)
                },
                onDeletePersona = { index ->
                    agregarComandaViewModel.eliminarPersona(index)
                }
            )
            binding.rvPedidosPersonas.adapter = personaOrderAdapter
            personaOrderAdapter.notifyDataSetChanged()
        }

        agregarComandaViewModel.totalGeneral.observe(viewLifecycleOwner) { total ->
            binding.tvTotalGeneral.text = "$${"%.2f".format(total)}"
        }

        // Nombre del cliente
        binding.etNombreCliente.setText(agregarComandaViewModel.nombreCliente.value)
        binding.etNombreCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                agregarComandaViewModel.setNombreCliente(s.toString())
            }
        })

        // Tipo de pedido
        binding.rgTipoPedido.setOnCheckedChangeListener { _, checkedId ->
            val paraLlevar = checkedId == R.id.rb_para_llevar
            agregarComandaViewModel.setParaLlevar(paraLlevar)
        }

        // Observaciones
        binding.etObservaciones.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                agregarComandaViewModel.setObservaciones(s.toString())
            }
        })

        // Botones Cancelar y Guardar
        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnGuardar.setOnClickListener {
            val (comanda, detalles) = agregarComandaViewModel.construirComanda()
            val comandasViewModel: ComandasViewModel by activityViewModels()
            comandasViewModel.addComanda(comanda, detalles)
            Toast.makeText(requireContext(), "Comanda guardada con folio ${comanda.folio}", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}