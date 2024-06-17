package com.kotlinrandomwordapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.kotlinrandomwordapp.R
import com.example.kotlinrandomwordapp.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var wordHandler: WordHandler? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        wordHandler = WordHandler(binding)
        val shakeAnim: Animation = AnimationUtils.loadAnimation(this.context, R.anim.shake)

        binding.mainEnterButton.setOnClickListener {
            val mainText: TextView = view.findViewById<TextView>(R.id.main_word_text)
            val scoreText: TextView = view.findViewById<TextView>(R.id.main_score_text)
            val userInput: EditText = view.findViewById<EditText>(R.id.main_text_entry_field)
            val enterButton: Button = view.findViewById<Button>(R.id.main_enter_button)

            viewLifecycleOwner.lifecycleScope.launch {
                val newNonGPTVars: Pair<String, Boolean> = withContext(Dispatchers.IO) {
                    wordHandler!!.handleUserEntry(userInput.text.toString(), mainText.text.toString())
                    wordHandler!!.getNonGPTVar()
                }
                scoreText.text = newNonGPTVars.first

                if (newNonGPTVars.second) {
                    view.findViewById<Button>(R.id.main_enter_button).setEnabled(false)
                    val newWord = withContext(Dispatchers.IO) {
                        wordHandler!!.getNewWord(userInput.text.toString())
                    }
                    mainText.text = newWord
                    view.findViewById<Button>(R.id.main_enter_button).setEnabled(true)
                    wordHandler!!.restartTimer()
                } else {
                    userInput.startAnimation(shakeAnim)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}