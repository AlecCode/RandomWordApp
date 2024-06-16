package com.kotlinrandomwordapp

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.kotlinrandomwordapp.R
import com.example.kotlinrandomwordapp.databinding.FragmentFirstBinding

// TODO: Refactor this
/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val dictionaryService: DictionaryService = DictionaryService()

    // TODO: Remove when ChatGPT api is added
    private val words: List<String> = mutableListOf<String>("Apple","Earl","Lamb","Banana")
    private var score: Int = 0

    private var timerStart : Boolean = false
    private val timer = object: CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.mainTimerText.text = (millisUntilFinished / 1000 + 1).toString()
        }

        override fun onFinish() {
            binding.mainTimerText.text = "Time Up!"
            binding.mainScoreText.text = "0"
            score = 0
            timerStart = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)

        binding.mainEnterButton.setOnClickListener {
            handleUserEntry(view)
        }
    }

    private fun handleUserEntry(view: View): Unit {
        val mainText: TextView = view.findViewById<TextView>(R.id.main_word_text)
        val scoreText: TextView = view.findViewById<TextView>(R.id.main_score_text)
        val userInput: EditText = view.findViewById<EditText>(R.id.main_text_entry_field)
        val isValid = dictionaryService.isInDictionary(userInput.text.toString())

        if ((mainText.text.isEmpty()
                    || mainText.text.last().equals(userInput.text.first(), ignoreCase=true))
            && isValid) {
            timer.start()
            score++
            scoreText.text = score.toString()
        } else {
            // TODO: Figure out how to show invalid answer
        }

        // TODO: Replace new word generation with ChatGPT call
        if (isValid) {
            for (word in words) {
                if (word[0].lowercase() == userInput.text[userInput.text.length - 1].lowercase()) {
                    mainText.text = word
                    break
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}