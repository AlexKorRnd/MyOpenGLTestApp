package com.example.myapplication2

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Trace
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.widget.TextView
import com.example.myapplication2.tg.SpoilerThread

/**
 * @author a.s.korchagin
 */

class ParticleTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    //val textView: TextView
) : TextureView(context, attrs), TextureView.SurfaceTextureListener {

    var thread: SpoilerThread? = null

    init {
        surfaceTextureListener = this
        isOpaque = false
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        setMeasuredDimension(textView.width, textView.height)
//    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (thread == null) {
            thread = SpoilerThread(surface, width, height) {
               // textView.invalidate()
                this.invalidate()
            }
            thread!!.start()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        thread?.updateSize(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (thread != null) {
            thread?.halt()
            thread = null
        }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

}
