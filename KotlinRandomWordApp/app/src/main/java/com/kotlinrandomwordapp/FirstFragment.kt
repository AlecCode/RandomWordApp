package com.kotlinrandomwordapp

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.kotlinrandomwordapp.R
import com.example.kotlinrandomwordapp.databinding.FragmentFirstBinding
import com.kotlinrandomwordapp.constants.INPUT_TOO_SOON
import com.kotlinrandomwordapp.constants.NOT_IN_DICTIONARY
import com.kotlinrandomwordapp.constants.NOT_VALID_CHAIN
import com.kotlinrandomwordapp.constants.TOO_RECENT_WORD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val timer = object: CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.mainTimerText.text = (millisUntilFinished / 1000).toString()
        }

        override fun onFinish() {
            binding.mainTimerText.text = "Time!"
            binding.mainScoreText.text = "0"
            binding.mainWordText.text = ""
            binding.mainWordHistory.text = ""
            wordHandler.reset()
        }
    }

    private var wordHandler: WordHandler = WordHandler()
    private var gptInFlight: Boolean = false
    private var animations: MutableMap<String, Animation> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        animations["shake"] = AnimationUtils.loadAnimation(this.context, R.anim.shake)
        animations["fadeIn"] = AnimationUtils.loadAnimation(this.context, R.anim.fade_in)
        animations["fadeOut"] = AnimationUtils.loadAnimation(this.context, R.anim.fade_out)

        binding.mainTextEntryField.setFocusableInTouchMode(true)
        binding.mainTextEntryField.requestFocus()
        binding.mainTextEntryField.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionID, _ ->
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    onEnterFromKeyboard(view)
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

    private fun onEnterFromKeyboard(view: View): Unit {
        val mainText: TextView = view.findViewById<TextView>(R.id.main_word_text)
        val scoreText: TextView = view.findViewById<TextView>(R.id.main_score_text)
        val userInput: EditText = view.findViewById<EditText>(R.id.main_text_entry_field)

        // Don't take new input if the GPT request is still in flight
        if (gptInFlight) {
            userInput.startAnimation(animations["shake"])
            showWarningText(view)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                wordHandler.handleUserEntry(userInput.text.toString(), mainText.text.toString())
            }

            scoreText.text = wordHandler.getScore().toString()

            if (wordHandler.getCombinedFlags()) {
                gptInFlight = true
                mainText.text = withContext(Dispatchers.IO) {
                    wordHandler.getNewWord(userInput.text.toString())
                }
                userInput.text.clear()
                gptInFlight = false

                timer.start()
            } else {
                userInput.startAnimation(animations["shake"])
                showWarningText(view)
            }

            if (!wordHandler.getIsNew()) {
                showWordHistory(view, userInput.text.toString())
            } else {
                showWordHistory(view, "")
            }
        }
    }

    private fun showWarningText(view: View): Unit {
        val warningText: TextView = view.findViewById<TextView>(R.id.main_warning_text)
        val mainText: TextView = view.findViewById<TextView>(R.id.main_word_text)

        if (!wordHandler.getInDictionary()) {
            warningText.text = NOT_IN_DICTIONARY
        } else if (!wordHandler.getIsValidChain()) {
            val chainMsg: String = NOT_VALID_CHAIN + mainText.text.last().uppercase() + "."
            warningText.text = chainMsg
        } else if (!wordHandler.getIsNew()) {
            warningText.text = TOO_RECENT_WORD
        } else {
            warningText.text = INPUT_TOO_SOON
        }

        warningText.visibility = View.VISIBLE
        warningText.startAnimation(animations["fadeIn"])
        warningText.startAnimation(animations["fadeOut"])
        warningText.visibility = View.INVISIBLE
    }

    private fun showWordHistory(view: View, highlight: String): Unit {
        val historyText: TextView = view.findViewById<TextView>(R.id.main_word_history)
        val history: ArrayDeque<String> = wordHandler.getWordHistory()

        var wordString: String = ""
        for (i in history) {
            val formattedWord: String = i.first().uppercase() + i.substring(1..<i.length)
            wordString = "$formattedWord $wordString"
        }

        if (highlight.isEmpty()) {
            historyText.text = wordString
        } else {
            val span: Spannable = wordString.toSpannable()
            val start: Int = wordString.lowercase().indexOf(highlight.lowercase())

            span.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                start + highlight.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            historyText.setText(span, TextView.BufferType.SPANNABLE)
        }
    }
}