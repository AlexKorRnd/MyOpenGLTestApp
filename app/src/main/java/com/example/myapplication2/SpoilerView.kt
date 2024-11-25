package com.example.myapplication2

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

/**
 * @author a.s.korchagin
 */
class SpoilerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var textureView: ParticleTextureView? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        textureView?.draw(canvas)
        textureView?.bitmap
        canvas.restore()
    }
}
