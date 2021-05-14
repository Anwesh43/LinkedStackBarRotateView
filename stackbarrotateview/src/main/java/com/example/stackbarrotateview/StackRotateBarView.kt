package com.example.stackbarrotateview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val colors : Array<Int> = arrayOf(
    "",
    "",
    "",
    "",
    ""
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

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}