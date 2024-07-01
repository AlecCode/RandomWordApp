package com.kotlinrandomwordapp

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
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
import com.kotlinrandomwordapp.constants.EMPTY_ENTRY
import com.kotlinrandomwordapp.constants.GAME_TIME
import com.kotlinrandomwordapp.constants.INPUT_TOO_SOON
import com.kotlinrandomwordapp.constants.LONGER_INPUT
import com.kotlinrandomwordapp.constants.NOT_IN_DICTIONARY
import com.kotlinrandomwordapp.constants.NOT_VALID_CHAIN
import com.kotlinrandomwordapp.constants.TICK
import com.kotlinrandomwordapp.constants.TIME_UP
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
    private val timer = object: CountDownTimer(GAME_TIME, TICK) {
        override fun onTick(millisUntilFinished: Long) {
            binding.timerTextView.text = (millisUntilFinished / 1000 + 1).toString()
            binding.timerProgressBar.progress = ((millisUntilFinished * 100) / GAME_TIME).toInt()
        }

        override fun onFinish() {
            sounds["bell"]?.start()
            wordHandler.reset()

            binding.timerTextView.text = TIME_UP
            binding.scoreTextView.text = "0"
            binding.generatedWordTextView.text = ""
            binding.hintTextView.text = ""
            binding.wordHistoryTextView.text = ""
            binding.timerProgressBar.progress = 0
        }

        fun reset() {
            binding.timerProgressBar.progress = 100
            binding.timerTextView.text = (GAME_TIME / 1000).toString()
            cancel()
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

        roundViewCorners()
        binding.userEntryEditText.setFocusableInTouchMode(true)
        binding.userEntryEditText.requestFocus()
        binding.userEntryEditText.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionID, _ ->
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    handleUserEntry()
                    return@OnEditorActionListener true
                }
                false
            }
        )
    }

    private fun roundViewCorners() {
        var mOutlineProvider: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, resources.displayMetrics).toInt()

                outline.setRoundRect(0, 0 - cornerRadius, view.width, view.height, 32f)
            }
        }
        binding.topBarCluster.apply {
            outlineProvider = mOutlineProvider
            clipToOutline = true
        }

        mOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 128f)
            }
        }
        binding.entryCluster.apply {
            outlineProvider = mOutlineProvider
            clipToOutline = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleUserEntry(): Unit {
        val mainText: TextView = binding.generatedWordTextView
        val scoreText: TextView = binding.scoreTextView
        val userInput: EditText = binding.userEntryEditText

        // Don't take new input if the GPT request is still in flight
        if (gptInFlight || userInput.text.isEmpty()) {
            showUserError()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                wordHandler.handleUserEntry(userInput.text.toString(), mainText.text.toString())
            }

            scoreText.text = wordHandler.score.toString()

            if (wordHandler.getCombinedFlags()) {
                sounds["pop"]?.start()
                timer.reset()
                if (wordHandler.longer) showHintText()

                gptInFlight = true
                mainText.text = withContext(Dispatchers.IO) {
                    wordHandler.getNewWord(userInput.text.toString())
                }
                gptInFlight = false

                timer.start()
            } else {
                showUserError()
            }

            showWordHistory(userInput.text.toString(), !wordHandler.isNew)
            userInput.text.clear()
        }
    }

    private fun showUserError(): Unit {
        sounds["error"]?.start()
        binding.userEntryEditText.startAnimation(animations["shake"])
        showHintText()
        vibratePhone()
    }

    private fun showHintText(): Unit {
        val hintText: TextView = binding.hintTextView
        val mainText: TextView = binding.generatedWordTextView

        if (wordHandler.longer) {
            hintText.text = LONGER_INPUT
        } else if (binding.userEntryEditText.text.isEmpty()) {
            hintText.text = EMPTY_ENTRY
        } else if (!wordHandler.inDictionary) {
            hintText.text = NOT_IN_DICTIONARY
        } else if (!wordHandler.isValidChain) {
            val chainMsg: String = NOT_VALID_CHAIN + mainText.text.last().uppercase() + "."
            hintText.text = chainMsg
        } else if (!wordHandler.isNew) {
            hintText.text = TOO_RECENT_WORD
        } else {
            hintText.text = INPUT_TOO_SOON
        }

        hintText.visibility = View.VISIBLE
        hintText.startAnimation(animations["fadeIn"])
        hintText.startAnimation(animations["fadeOut"])
        hintText.visibility = View.INVISIBLE
    }

    private fun showWordHistory(highlight: String, doHighlight: Boolean): Unit {
        val historyText: TextView = binding.wordHistoryTextView
        val history: ArrayDeque<String> = wordHandler.wordHistory

        var wordString: String = ""
        for (i in history) {
            val formattedWord: String = i.first().uppercase() + i.substring(1..<i.length)
            wordString = "$formattedWord $wordString"
        }

        if (doHighlight) {
            val span: Spannable = wordString.toSpannable()
            val start: Int = wordString.lowercase().indexOf(highlight.lowercase())

            span.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                start + highlight.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            historyText.setText(span, TextView.BufferType.SPANNABLE)
        } else {
            historyText.text = wordString
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