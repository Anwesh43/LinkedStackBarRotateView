package com.example.stackbarrotateview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import java.util.*

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#3F51B5",
    "#00C853",
    "#304FFE",
    "#880E4F"
).map {
    Color.parseColor(it)
}.toTypedArray()
val bars : Int = 3
val parts : Int = 1 + bars
val scGap : Float = 0.02f / parts
val recHFactor : Float = 3.9f
val recWFactor : Float = 7.8f
val delay : Long = 20
val rot : Float = 90f
val backColor : Int = Color.parseColor("#BDBDBD")


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawStackRotateBar(scale : Float, w : Float, h : Float, paint : Paint) {
    val rw : Float = Math.min(w, h) / recWFactor
    val rh : Float = Math.min(w, h) / recHFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    save()
    translate(w / 2, 0f)
    for (j in 0..(bars - 1)) {
        val sfj : Float = sf.divideScale(j + 1, parts)
        save()
        translate(0f, rh / 2 + (h - rw / 2 - rh / 2) * sfj)
        rotate(rot * sfj)
        drawRect(RectF(-rw / 2, -rh / 2, rw / 2, -rh / 2 + rh * sf1), paint)
        restore()
    }
    restore()
}

fun Canvas.drawSRBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    drawStackRotateBar(scale, w, h, paint)
}

class StackRotateBarView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SRBNode(var i : Int, val state : State = State()) {

        private var next : SRBNode? = null
        private var prev : SRBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SRBNode(i +  1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSRBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SRBNode {
            var curr : SRBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class StackRotateBar(var i : Int) {

        private var curr : SRBNode = SRBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : StackRotateBarView) {

        private val animator : Animator = Animator(view)
        private val srb : StackRotateBar = StackRotateBar(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            srb.draw(canvas, paint)
            animator.animate {
                srb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            srb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity: Activity) : StackRotateBarView {
            val view : StackRotateBarView = StackRotateBarView(activity)
            activity.setContentView(view)
            return view
        }
    }
}