package com.rega.heallink.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rega.heallink.R
import com.rega.heallink.ViewModelFactory
import com.rega.heallink.databinding.FragmentListBinding
import com.rega.heallink.di.Injection

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter

    private val listViewModel: ListViewModel by viewModels {
        ViewModelFactory(Injection.provideNoteRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fabAdd.setOnClickListener { view ->
            view.findNavController().navigate(R.id.noteAddFragment)
        }

        adapter = NoteAdapter()
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        observeViewModel()
        return root
    }

    private fun observeViewModel() {
        listViewModel.getAllNotes()
        listViewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.setNotes(notes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}