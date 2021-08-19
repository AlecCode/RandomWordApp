package com.example.kotlinrandomwordapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.kotlinrandomwordapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val words = mutableListOf<String>("Apple","Earl","Lamb","Banana")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.firstFragmentButtonNext.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.firstFragmentButtonEnter.setOnClickListener {
            giveRandomWord(view)
        }
    }

    private fun giveRandomWord(view: View) {
        val showRandomWordTextView = view.findViewById<TextView>(R.id.first_fragment_textview_random_word)
        val userInput = view.findViewById<EditText>(R.id.first_fragment_edit_text_word_entry).text
        val random = java.util.Random()
        var hasValidWord = false

        for (word in words) {
            if (word[0].lowercase() == userInput[userInput.length - 1].lowercase()) {
                hasValidWord = true
                showRandomWordTextView.text = word
                break
            }
        }

        if (!hasValidWord) {
            var randomIndex = random.nextInt(words.size)
            showRandomWordTextView.text = words[randomIndex]
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}