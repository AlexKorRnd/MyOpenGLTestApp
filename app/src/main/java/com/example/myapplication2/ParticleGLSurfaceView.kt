package com.example.myapplication2

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class ParticleGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GLSurfaceView(context, attrs) {



    init {
        // Устанавливаем OpenGL ES 2.0
        setEGLContextClientVersion(2)
        setRenderer(ParticleRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}