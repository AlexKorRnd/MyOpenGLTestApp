package com.example.myapplication2

import android.graphics.RectF
import androidx.annotation.FloatRange
import kotlin.random.Random

/**
 * @author k.shiryaev
 */
internal interface ParticlesGenerator<Particle> {

    var bounds: RectF

    val particles: List<Particle>

    fun fill()

    fun update()
}

internal class ParticlesGeneratorImpl(
) : ParticlesGenerator<Particle> {

    override var bounds: RectF = RectF()

    override var particles: List<Particle> = emptyList()
        private set

    override fun fill() {
        val countOfParticles = 1000

        particles = buildList {
            repeat(countOfParticles.toInt()) {
                add(getNewRandomParticle())
            }
        }
    }

    override fun update() {
        particles = particles.map { particle ->
            when (particle.direction) {
                Particle.Direction.RIGHT -> particle.copy(
                    x = if (particle.x >= bounds.width() + particle.size * 2) {
                        particle.x - PARTICLE_X_DIRECTION_SPEED
                    } else {
                        particle.x + PARTICLE_X_DIRECTION_SPEED
                    },
                    y = getNewYPosition(particle.y),
                    direction = if (particle.x >= bounds.width()) {
                        Particle.Direction.LEFT
                    } else {
                        Particle.Direction.RIGHT
                    },
                )

                Particle.Direction.LEFT -> particle.copy(
                    x = if (particle.x <= 0 - particle.size) {
                        particle.x + PARTICLE_X_DIRECTION_SPEED
                    } else {
                        particle.x - PARTICLE_X_DIRECTION_SPEED
                    },
                    y = getNewYPosition(particle.y),
                    direction = if (particle.x <= 0) {
                        Particle.Direction.RIGHT
                    } else {
                        Particle.Direction.LEFT
                    },
                )
            }
        }
    }

    private fun getNewRandomParticle(): Particle = Particle(
        x = Random.nextFloat(bounds.left, bounds.width()),
        y = Random.nextFloat(bounds.top, bounds.height()),
        size = Random.nextFloat(MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE),
        alpha = Random.nextFloat(MIN_ALPHA_FOR_PARTICLE, MAX_ALPHA_FOR_PARTICLE),
        direction = if (Random.nextFloat() > 0.5f) {
            Particle.Direction.RIGHT
        } else {
            Particle.Direction.LEFT
        }
    )

    private fun getNewYPosition(oldY: Float): Float = when {
        oldY < bounds.top -> oldY + Random.nextFloat() * PARTICLE_Y_DIRECTION_SPEED
        oldY > bounds.bottom -> oldY - Random.nextFloat() * PARTICLE_Y_DIRECTION_SPEED
        else -> oldY + getRandomMinusOrPlus() * PARTICLE_Y_DIRECTION_SPEED
    }

    private fun getRandomMinusOrPlus(): Int = if (Random.nextFloat() > 0.5f) 1 else -1

    private fun Random.nextFloat(min: Float, max: Float) = min + nextFloat() * (max - min)

    private companion object {
        const val MAX_PARTICLE_SIZE = 2.5f
        const val MIN_PARTICLE_SIZE = 1.5f
        const val MIN_ALPHA_FOR_PARTICLE = 0.3f
        const val MAX_ALPHA_FOR_PARTICLE = 0.8f
        const val PARTICLE_X_DIRECTION_SPEED = 0.5f
        const val PARTICLE_Y_DIRECTION_SPEED = 0.78f

        /**
         * Коэффициент заполнения частицами, выведен путем перебора. Чем ниже число,
         * тем меньше частиц будет заполнено
         */
        const val FILL_FACTOR = 0.02f
    }
}

internal data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float,
    val direction: Direction,
) {

    enum class Direction {
        RIGHT,
        LEFT,
    }
}
