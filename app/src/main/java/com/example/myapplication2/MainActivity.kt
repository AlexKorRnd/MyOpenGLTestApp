package com.example.myapplication2

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.toSpannable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.example.myapplication2.tg.SpoilerEffect
import java.util.Stack

class MainActivity : AppCompatActivity() {

    val spoilerText = "132 999 999 %"
    //val customText = "Какой-то текст $spoilerText"

    protected var spoilers: List<SpoilerEffect> = ArrayList()
    private val spoilersPool: Stack<SpoilerEffect> = Stack<SpoilerEffect>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewContainer: LinearLayout = findViewById(R.id.viewContainer)

        findViewById<Button>(R.id.glSurfaceBtn).setOnClickListener {
            createGLViews(viewContainer)
        }
        findViewById<Button>(R.id.textureViewBtn).setOnClickListener {
            createTextureViews(viewContainer)
        }
        findViewById<Button>(R.id.drawableBtn).setOnClickListener {
            createTextViews(viewContainer)
            viewContainer.children.forEach { child ->
                if (child is TextView) {
                    child.tuiSensitiveWrapper(isMasked = true)
                    SpoilerEffect.addSpoilers(child, spoilersPool, spoilers)
                }
            }
        }
    }

    private fun createTextureViews(container: LinearLayout) {
        container.removeAllViews()
        for (i in 0 until TOTAL_COUNT_TEXTURES) {
            val view = ParticleTextureView(container.context)
            view.layoutParams = LinearLayout.LayoutParams(800, 150).apply {
                topMargin = 30
            }
            container.addView(view)
        }
    }

    private fun createGLViews(container: LinearLayout) {
        container.removeAllViews()
        for (i in 0 until TOTAL_COUNT_TEXTURES) {
            val view = ParticleGLSurfaceView(container.context)
            view.layoutParams = LinearLayout.LayoutParams(800, 150).apply {
                topMargin = 30
            }
            container.addView(view)
        }
    }

    private fun createTextViews(container: LinearLayout) {
        container.removeAllViews()
        for (i in 0 until TOTAL_COUNT_VIEWS) {
            val view = TextView(container.context)
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
            view.textSize = DEFAULT_TEXT_SIZE
            view.text = spoilerText
            container.addView(view)
        }
    }
}

fun TextView.tuiSensitiveWrapper(
    isMasked: Boolean,
    @ColorInt color: Int = currentTextColor,
    sensitiveSubstring: String? = null,
) {
    var startIndex = 0
    var endIndex = text.length
    if (sensitiveSubstring != null) {
        startIndex = text.indexOf(sensitiveSubstring)
        if (startIndex == -1) {
            return
        }
        endIndex = startIndex + sensitiveSubstring.length
    }
    val newText = text.toSpannable()
    newText.pixelMaskInternal(
        isMasked = isMasked,
        view = this,
        color = color,
        start = startIndex,
        end = endIndex,
    )
    if (ellipsize != null) {
        text = newText
    } else {
        setText(newText, TextView.BufferType.SPANNABLE)
    }

}

internal fun <T : Spannable> T.pixelMaskInternal(
    isMasked: Boolean,
    view: TextView,
    start: Int,
    end: Int,
    @ColorInt color: Int,
    flags: Int = Spannable.SPAN_INCLUSIVE_INCLUSIVE,
): T {
    if (isMasked) {
        val drawableSpan = SpoilerSpannable(view).apply {
            show()
        }
        setSpan(drawableSpan, start, end, flags)

    } else {
        getSpans(start, end, Spannable::class.java).forEach { drawableSpan ->
            removeSpan(drawableSpan)
        }
    }

    return this
}

const val DEFAULT_TEXT_SIZE = 28F
const val TOTAL_COUNT_TEXTURES = 10
const val TOTAL_COUNT_VIEWS = 10

