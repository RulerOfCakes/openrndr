import org.openrndr.animatable.Animatable
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

fun main() = application {
    configure {
        width = 1000
        height = 1000
    }
    program {
        val nodeCount = 10
        fun randomVector(
            minX: Double = width * 0.2,
            maxX: Double = width * 0.8,
            minY: Double = height * 0.2,
            maxY: Double = height * 0.8
        ): Vector2 =
            Vector2(random(minX, maxX), random(minY, maxY))

        class Node(
            val id: Int, val v: Vector2, val r: Double = 5.0, val color: ColorRGBa = ColorRGBa(1.0, 1.0, 1.0, 1.0)
        ) {

            fun show() {
                drawer.stroke = null
                drawer.fill = color
                drawer.circle(v, r)
                drawer.fill = ColorRGBa.RED

                drawer.text("${id}", v)
            }
        }

        class Line(
            val from: Node, val to: Node, val color: ColorRGBa = ColorRGBa.WHITE
        ) : Animatable() {
            var start = Vector2.ZERO
            var end = Vector2.ZERO
            var done = false
            fun show() {
                if (done) {
                    drawer.stroke = ColorRGBa.BLUE
                    drawer.strokeWeight = 2.0
                    drawer.lineSegment(from.v, to.v)
                    return
                }

                drawer.stroke = color
                drawer.strokeWeight = 2.0
                drawer.lineSegment(from.v, to.v)
                if (this.hasAnimations()) {
                    drawer.stroke = ColorRGBa.RED
                    drawer.lineSegment(start, end)
                }
            }

            fun update(begin: Vector2, target: Vector2) {
                updateAnimation()
                if (!hasAnimations()) {
                    cancel()
                    start = begin
                    end = begin

                    var now = clock().time
                    animate(::end, target, 1000L).completed.listen {
                        println("done in ${clock().time - now}")
                        done = true
                    }
                }
            }
        }

        val arr = Array(nodeCount) { Node(it, randomVector()) }
        val adj = Array(nodeCount) { mutableListOf<Line>() }
        val lines = mutableListOf<Line>()
        var animations = mutableListOf<Line>()
        for (i in 0 until nodeCount) {
            for (j in i + 1 until nodeCount) {
                val rd = Random.int(0, 3)
                if (rd == 1) {
                    lines.add(Line(arr[i], arr[j]))
                    adj[i].add(lines.last())
                    adj[j].add(lines.last())
                }
            }
            if (adj[i].isEmpty()) {
                val rd = Random.int(0, nodeCount)
                lines.add(Line(arr[i], arr[rd]))
                adj[i].add(lines.last())
                adj[rd].add(lines.last())
            }
        }
        for (line in adj[0]) {
            if (line.from == arr[0])
                line.update(line.from.v, line.to.v)
            else line.update(line.to.v, line.from.v)
            animations.add(line)
        }
        // extend(NoClear())
        extend {
            drawer.stroke = null
            // drawer.clear(ColorRGBa(0.0, 0.0, 0.0))
            drawer.fill = (ColorRGBa.WHITE)

            for (l in lines) l.show()
            for (n in arr) n.show()
            val newAnimations = mutableListOf<Line>()
            for (a in animations) {
                a.updateAnimation()
                if (a.done) {
                    val node = if (a.end == a.from.v) a.from else a.to
                    for (line in adj[node.id]) {
                        if (line.hasAnimations() || line.done) continue
                        if (line.from.v == node.v)
                            line.update(line.from.v, line.to.v)
                        else line.update(line.to.v, line.from.v)

                        newAnimations.add(line)
                    }
                } else newAnimations.add(a)
            }
            animations = newAnimations
            // println(animations.size)
        }
    }
}