package com.example.myapplication2

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class ParticleExplosionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateParticles()
            invalidate()
            updateHandler.postDelayed(this, 16) // Обновление каждые 16 мс (~60 FPS)
        }
    }

    // Метод для генерации частиц
    private fun generateParticles(centerX: Float, centerY: Float) {
        particles.clear()


        for (i in 0 until 100) { // Генерация 100 частиц
            val x = Random.nextInt(width).toFloat()
            val y = Random.nextInt(height).toFloat()
            val radius = 2F // Радиус от 5 до 15
            val speedX = (Random.nextFloat() - 0.5f) * 10 // Скорость по X от -5 до 5
            val speedY = (Random.nextFloat() - 0.5f) * 10 // Скорость по Y от -5 до 5
            val color = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

            //particles.add(Particle(x, y, radius, speedX, speedY, color))
        }
    }

    // Метод для начала обновления частиц
    fun startUpdatingParticles() {
        generateParticles(width / 2f, height / 2f)
        updateHandler.post(updateRunnable)
    }

    // Метод для остановки обновления частиц
    fun stopUpdatingParticles() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    // Обновление позиций частиц и обработка столкновений
    private fun updateParticles() {
        for (i in particles.indices) {
            val particle = particles[i]

            // Обновляем позицию
//            particle.x += particle.speedX
//            particle.y += particle.speedY
//
//            // Проверка столкновения с границами View
//            if (particle.x - particle.radius < 0 || particle.x + particle.radius > width) {
//                particle.speedX = -particle.speedX // Меняем направление по X
//            }
//            if (particle.y - particle.radius < 0 || particle.y + particle.radius > height) {
//                particle.speedY = -particle.speedY // Меняем направление по Y
//            }
//
//            // Проверка столкновения с другими частицами
//            for (j in i + 1 until particles.size) {
//                val otherParticle = particles[j]
//                val distance = sqrt((particle.x - otherParticle.x).pow(2) + (particle.y - otherParticle.y).pow(2))
//                if (distance <= particle.radius + otherParticle.radius) {
//                    // Если частицы сталкиваются, меняем направление их движения
//                    val tempSpeedX = particle.speedX
//                    val tempSpeedY = particle.speedY
//                    particle.speedX = otherParticle.speedX
//                    particle.speedY = otherParticle.speedY
//                    otherParticle.speedX = tempSpeedX
//                    otherParticle.speedY = tempSpeedY
//                }
//            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисование всех частиц
        for (particle in particles) {
//            paint.color = particle.color
//            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
        }
    }
}