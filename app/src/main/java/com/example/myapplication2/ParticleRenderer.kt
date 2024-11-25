package com.example.myapplication2


import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.FloatBuffer
import java.util.Random
import javax.microedition.khronos.opengles.GL10
import kotlin.text.Typography.cent

class ParticleRenderer : GLSurfaceView.Renderer {

    private val particlesGenerator: ParticlesGenerator<Particle> = ParticlesGeneratorImpl()

    private val particles
        get() = particlesGenerator.particles
    private var program = 0

    // Шейдеры
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        uniform float uPointSize;
        void main() {
            gl_Position = vPosition;
            gl_PointSize = uPointSize;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

    private var positionHandle = 0
    private var colorHandle = 0
    private var pointSizeHandle = 0
    private var vbo = IntArray(1)


    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(1F, 1F, 1F, 1F)

        // Генерация и компиляция шейдеров
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Создание программы и связывание шейдеров
        program = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }

        // Генерация VBO
        GLES20.glGenBuffers(1, vbo, 0)

        // Генерация частиц
        generateParticles(RectF())
    }

    override fun onDrawFrame(gl: GL10?) {
        val color = Color.BLACK
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Используем программу шейдеров
        GLES20.glUseProgram(program)

        // Обновляем координаты частиц
        updateParticles()

        // Привязываем VBO и записываем данные
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        val particleData = particles.flatMap { it.coordinates() }.toFloatArray()
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, particleData.size * 3,
            FloatBuffer.wrap(particleData), GLES20.GL_DYNAMIC_DRAW
        )

        // Получаем атрибуты
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        pointSizeHandle = GLES20.glGetUniformLocation(program, "uPointSize")

        // Устанавливаем атрибуты и параметры шейдера
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, 0)

        //GLES20.glUniform4f(colorHandle, red, green, blue, alpha)

        particles.forEachIndexed { index, particle ->
            val red = (color shr 16 and 0xFF) / 255F
            val green = (color shr 8 and 0xFF) / 255F
            val blue = (color and 0xFF) / 255F
            val alpha = particle.alpha * 255F
            GLES20.glUniform4f(colorHandle, red, green, blue, alpha)
            GLES20.glUniform1f(pointSizeHandle, particle.size * 3) // Размер точки
            // Рисуем точки
            GLES20.glDrawArrays(GLES20.GL_POINTS, index, 1)
        }

        // Отключаем атрибуты
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun Particle.coordinates(): List<Float> {
        val centerX = particlesGenerator.bounds.centerX()
        val centerY = particlesGenerator.bounds.centerY()
        val newX = if (centerX == 0F) 0F else x / centerX - 1F
        val newY = if (centerY == 0F) 0F else y / centerX - 1F
        return listOf(newX, newY)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        generateParticles(RectF(0F, 0F, width.toFloat(), height.toFloat()))
        GLES20.glViewport(0, 0, width, height)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).apply {
            GLES20.glShaderSource(this, shaderCode)
            GLES20.glCompileShader(this)
        }
    }

    private fun generateParticles(bounds: RectF) {
        particlesGenerator.bounds = bounds
        particlesGenerator.fill()
//        val random = Random()
//        for (i in 0 until 1000) {
//            val x = random.nextFloat() * 2.0f - 1.0f // Значения от -1.0 до 1.0
//            val y = random.nextFloat() * 2.0f - 1.0f
//            val z = 0.0f
//            val speedX = (random.nextFloat() - 0.5f) * 0.02f
//            val speedY = (random.nextFloat() - 0.5f) * 0.02f
//            particles.add(Particle(x, y, z, speedX, speedY))
//        }
    }

    private fun updateParticles() {
        particlesGenerator.update()
//        for (particle in particles) {
//            particle.x += particle.speedX
//            particle.y += particle.speedY
//
//            // Проверка столкновения с границами экрана
//            if (particle.x < -1.0f || particle.x > 1.0f) {
//                particle.speedX = -particle.speedX
//            }
//            if (particle.y < -1.0f || particle.y > 1.0f) {
//                particle.speedY = -particle.speedY
//            }
//        }
    }
}
