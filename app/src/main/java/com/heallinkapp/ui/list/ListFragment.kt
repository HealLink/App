package com.heallinkapp.ui.list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heallinkapp.R
import com.heallinkapp.ViewModelFactory
import com.heallinkapp.data.UserRepository
import com.heallinkapp.databinding.FragmentListBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.ui.add.AddActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter

    private val listViewModel: ListViewModel by viewModels {
        ViewModelFactory(Injection.provideNoteRepository(requireContext()))
    }
    private lateinit var userRepository: UserRepository

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddActivity::class.java)
            startActivity(intent)
        }

        userRepository = Injection.provideUserRepository(requireContext())

        lifecycleScope.launch {
            val username = userRepository.userName.firstOrNull() ?: "User"
            binding.textGreet.text = "Hi $username\nTell me your story!"
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