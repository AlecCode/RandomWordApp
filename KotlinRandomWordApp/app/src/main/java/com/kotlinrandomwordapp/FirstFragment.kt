package com.kotlinrandomwordapp

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
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
import com.kotlinrandomwordapp.constants.GAME_TIME
import com.kotlinrandomwordapp.constants.INPUT_TOO_SOON
import com.kotlinrandomwordapp.constants.INTERVAL
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
    private val timer = object: CountDownTimer(GAME_TIME, INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            binding.mainTimerText.text = (millisUntilFinished / INTERVAL).toString()
        }

        override fun onFinish() {
            sounds["bell"]?.start()
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
    private var sounds: MutableMap<String, MediaPlayer> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {
        super.onViewCreated(view, savedInstanceState)
        animations["shake"] = AnimationUtils.loadAnimation(this.context, R.anim.shake)
        animations["fadeIn"] = AnimationUtils.loadAnimation(this.context, R.anim.fade_in)
        animations["fadeOut"] = AnimationUtils.loadAnimation(this.context, R.anim.fade_out)
        sounds["pop"] = MediaPlayer.create(this.context, R.raw.pop)
        sounds["bell"] = MediaPlayer.create(this.context, R.raw.bell)
        sounds["error"] = MediaPlayer.create(this.context, R.raw.error)

        sounds["bell"]?.setVolume(0.1f, 0.1f)
        sounds["error"]?.setVolume(0.5f, 0.5f)

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
            vibratePhone()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                wordHandler.handleUserEntry(userInput.text.toString(), mainText.text.toString())
            }

            scoreText.text = wordHandler.getScore().toString()

            if (wordHandler.getCombinedFlags()) {
                sounds["pop"]?.start()

                gptInFlight = true
                mainText.text = withContext(Dispatchers.IO) {
                    wordHandler.getNewWord(userInput.text.toString())
                }
                userInput.text.clear()
                gptInFlight = false

                timer.start()
            } else {
                sounds["error"]?.start()
                userInput.startAnimation(animations["shake"])
                showWarningText(view)
                vibratePhone()
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

    private fun vibratePhone() {
        val vibrator: Vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 31) {
            vibrator.vibrate(
                VibrationEffect.startComposition().addPrimitive(
                    VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 20
                ).compose()
            )
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        }

    }
}