package com.example.mooqoo.myapplication

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth



class PointerDrawable : Drawable() {

    val paint: Paint = Paint()
    var enabled = false

    override fun draw(canvas: Canvas) {
        val cx = canvas.width / 2F
        val cy = canvas.height / 2F
        if (enabled) {
            paint.color = Color.GREEN
            canvas.drawCircle(cx, cy, 10F, paint)
        } else {
            paint.color = Color.GRAY
            canvas.drawText("X", cx, cy, paint)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun getOpacity(): Int {
        return 1
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }
}