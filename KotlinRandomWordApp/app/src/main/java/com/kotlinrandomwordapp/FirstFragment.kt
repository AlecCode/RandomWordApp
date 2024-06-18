package com.kotlinrandomwordapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
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
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var wordHandler: WordHandler? = null
    private var gptInFlight: Boolean = false

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

        binding.mainTextEntryField.setFocusableInTouchMode(true)
        binding.mainTextEntryField.requestFocus()
        binding.mainTextEntryField.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionID, _ ->
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    onEnterFromKeyboard(view, shakeAnim)
                    return@OnEditorActionListener true
                }
                false
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onEnterFromKeyboard(view: View, shakeAnim: Animation): Unit {
        val mainText: TextView = view.findViewById<TextView>(R.id.main_word_text)
        val scoreText: TextView = view.findViewById<TextView>(R.id.main_score_text)
        val userInput: EditText = view.findViewById<EditText>(R.id.main_text_entry_field)

        // Don't take new input if the GPT request is still in flight
        if (gptInFlight) return

        viewLifecycleOwner.lifecycleScope.launch {
            val newNonGPTVars: Pair<String, Boolean> = withContext(Dispatchers.IO) {
                wordHandler!!.handleUserEntry(userInput.text.toString(), mainText.text.toString())
                wordHandler!!.getNonGPTVar()
            }
            scoreText.text = newNonGPTVars.first

            if (newNonGPTVars.second) {
                gptInFlight = true
                mainText.text = withContext(Dispatchers.IO) {
                    wordHandler!!.getNewWord(userInput.text.toString())
                }
                userInput.text.clear()
                gptInFlight = false

                wordHandler!!.restartTimer()
            } else {
                userInput.startAnimation(shakeAnim)
            }
        }
    }
}