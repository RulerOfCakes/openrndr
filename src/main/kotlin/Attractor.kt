import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

// F = G*(M1+m2)/d^2

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }
    oliveProgram {
        val debugMode = false
        fun randomVector(
            minX: Double = width * 0.2,
            maxX: Double = width * 0.8,
            minY: Double = height * 0.2,
            maxY: Double = height * 0.8
        ): Vector2 =
            Vector2(random(minX, maxX), random(minY, maxY))

        class Particle(
            var v: Vector2, val r: Double = 5.0, val color: ColorRGBa = ColorRGBa(1.0, 1.0, 1.0, 0.1)
        ) {
            private var velocity = randomVector(-1.0, 1.0, -1.0, 1.0)
            private var acceleration = Vector2(0.0, 0.0)
            private fun update() {
                v += velocity
                velocity += acceleration
            }

            fun show(drawer: Drawer, targets: Array<Vector2>) {
                attracted(targets)
                update()
                drawer.fill = color
                drawer.circle(v, r)
                if (!debugMode) return
                drawer.fill = ColorRGBa.WHITE
                drawer.text("${velocity.length}", Vector2(v.x, v.y + 10.0))
            }

            fun attracted(targets: Array<Vector2>) {
                acceleration = Vector2.ZERO
                for (target in targets) {
                    var force = (target - v).normalized
                    val dsquared = v.squaredDistanceTo(target).coerceIn(100.0, 150.0)
                    val G = 10
                    val strength = G / dsquared
                    force *= strength
                    acceleration += force
                }
            }
        }

        val arr = Array<Particle>(100) { Particle(Vector2(400.0, 100.0)) }
        val attractors = Array(4) { randomVector() }
        // extend(NoClear())
        extend {
            drawer.stroke = null
            // drawer.clear(ColorRGBa.BLACK)
            drawer.fill = (ColorRGBa.WHITE)
            for (p in arr) p.show(drawer, attractors)
            for (a in attractors) {
                drawer.fill = ColorRGBa.CYAN
                drawer.circle(a, 5.0)
            }
        }
    }
}