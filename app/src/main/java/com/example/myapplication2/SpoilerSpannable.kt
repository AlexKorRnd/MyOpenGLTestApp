package com.example.myapplication2

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.text.style.ReplacementSpan
import android.widget.TextView
import androidx.core.graphics.withTranslation
import androidx.core.view.updateLayoutParams
import com.example.myapplication2.tg.SpoilerEffect
import com.example.myapplication2.tg.SpoilerEffectBitmapFactory

/**
 * @author a.s.korchagin
 */

private const val ANIMATION_DURATION_MS = 5_000L

class SpoilerSpannable(
    //val textureView: ParticleTextureView,
    val view: TextView
) : ReplacementSpan() {

    private val currentFontMetricsInt: Paint.FontMetricsInt = Paint.FontMetricsInt()
    private val textBounds = Rect()

    val drawable: SpoilerEffect = SpoilerEffect().apply {
        SpoilerEffectBitmapFactory.getInstance().setupSpoiler(this)
        //drawPoints = true
        //maxParticlesCount = 30
    }

    private val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        duration = ANIMATION_DURATION_MS
        addUpdateListener {
            view.invalidate()
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return setupDrawableBounds(paint, text, start, end)
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
//        val glCanvas = textureView.lockCanvas()
//        val bitmap = textureView.bitmap
//        glCanvas?.let {
//            canvas.save()
//            canvas.translate(x, top.toFloat())
//
//            //textureView.draw(canvas)
//
//            canvas.restore()
//        }
        canvas.withTranslation(x, top.toFloat()) {
            drawable.draw(canvas)
        }
    }


    private fun setupDrawableBounds(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
    ): Int {
        paint.getFontMetricsInt(currentFontMetricsInt)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            paint.getTextBounds(text, start, end, textBounds)
        } else {
            paint.getTextBounds(text.toString(), start, end, textBounds)
        }
        val textWidth = textBounds.width()
        val height = currentFontMetricsInt.descent - currentFontMetricsInt.ascent
//        if (textureView.height != height || textureView.width != textWidth) {
//            textureView.updateLayoutParams {
//                this.height = height
//                this.width = textWidth
//            }
//        }
        drawable.setBounds(0, 0, textWidth, height)
        return textWidth
    }

    fun show() {
        animator.start()
    }
}
