// import org.openrndr.extra.olive.oliveProgram
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */
object GeometryUtils {
    fun ccw(p1: Vector2, p2: Vector2): Int {
        val ret = p1.x * p2.y - p2.x * p1.y
        if (ret < 0)
            return -1
        else if (ret > 0)
            return 1
        else
            return 0
    }

    fun ccw(p1: Vector2, p2: Vector2, p3: Vector2): Int {
        return ccw(
            Vector2(p1.x - p3.x, p1.y - p3.y),
            Vector2(p2.x - p3.x, p2.y - p3.y)
        )
    }

    fun convex_hull(arr: Array<Vector2>): Array<Vector2> {
        if (arr.size <= 3) return emptyArray()
        val sortedArr = arr.sortedBy { it.x }

        val ret = mutableListOf<Vector2>()

        for (i in 0 until arr.size) {
            while (ret.size >= 2 && ccw(ret[ret.size - 2], ret[ret.size - 1], sortedArr[i]) <= 0) {
                ret.removeLast()
            }
            ret.add(sortedArr[i])
        }
        val t = ret.size + 1
        for (i in arr.size - 1 downTo 1) {
            while (ret.size >= t && ccw(ret[ret.size - 2], ret[ret.size - 1], sortedArr[i - 1]) <= 0) ret.removeLast()
            ret.add(sortedArr[i - 1])
        }
        return ret.toTypedArray()
    }
}

class AnimatableVector(var v: Vector2) : Animatable() {
    // constructor(x: Double, y: Double) : this(Vector2(x, y))
}


fun main() = application {

    configure {
        width = 800
        height = 800
        windowResizable = true
        title = "OPENRNDR example"
    }
    program {
        fun randomVector(minX: Double, maxX: Double, minY: Double, maxY: Double): Vector2 =
            Vector2(random(minX, maxX), random(minY, maxY))

        val pointCnt = 25
        // val tickSpeed = 3.0
        val arr = Array(pointCnt) { AnimatableVector(Vector2.ZERO) }

        repeat(pointCnt) {
            arr[it] = AnimatableVector(
                Vector2(
                    random(width.toDouble() * 0.1, width.toDouble() * 0.9),
                    random(height.toDouble() * 0.1, height.toDouble() * 0.9)
                )
            )
            /*            arr[it].apply {
                            ::v.animate(
                                randomVector(
                                    width.toDouble() * 0.1,
                                    width.toDouble() * 0.9,
                                    height.toDouble() * 0.1,
                                    height.toDouble() * 0.9
                                ), 5000, Easing.CubicInOut
                            )
                        }*/
        }
        // extend(ScreenRecorder())
        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = ColorRGBa.WHITE

            repeat(pointCnt) {
                drawer.circle(arr[it].v, 3.0)
                arr[it].updateAnimation()
                if (!arr[it].hasAnimations()) {
                    arr[it].apply {
                        ::v.animate(
                            randomVector(
                                width.toDouble() * 0.1,
                                width.toDouble() * 0.9,
                                height.toDouble() * 0.1,
                                height.toDouble() * 0.9
                            ), random(1000.0, 5000.0).toLong(), Easing.CubicInOut
                        )
                        ::v.complete()
                    }
                }
            }
            drawer.stroke = ColorRGBa.GRAY
            drawer.strokeWeight = 2.0
            drawer.lineCap = LineCap.ROUND
            val convexHull = GeometryUtils.convex_hull(arr.map { it.v }.toTypedArray())
            for (i in 1 until convexHull.size) {
                drawer.lineSegment(convexHull[i - 1], convexHull[i])
            }
        }
    }
}