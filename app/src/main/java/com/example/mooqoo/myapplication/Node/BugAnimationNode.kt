package com.example.mooqoo.myapplication.Node

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Handler
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import java.util.*
import kotlin.concurrent.schedule

class BugAnimationNode : Node() {
    private val ANIMATION_DURATION = 500L
    private val ROTATION_DURATION = 4000L
    private val ORBIT_DURATION = 8000L

    private val SLOW_RATIO = 2L

//    var animator: ObjectAnimator? = null
    // TODO animation state
    var movementState: MOVEMENT = MOVEMENT.JUMP

    private fun createFlyAnimator(duration: Long = ANIMATION_DURATION, repeat: Int = 5, vararg vectors: Vector3): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()

        objectAnimator.apply {
            target = this@BugAnimationNode
            this.duration = duration
            propertyName = "localPosition"
            repeatCount = repeat // ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()

            setObjectValues(*vectors)
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
        return objectAnimator
    }

    private fun createRotateAnimator(duration: Long = ROTATION_DURATION, repeat: Int = 0, vararg quaternions: Quaternion): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()
        objectAnimator.apply {
            target = this@BugAnimationNode
            this.duration = duration
            propertyName = "localRotation"  // TODO update
            repeatCount = if (repeat == -1) ObjectAnimator.INFINITE else repeat
            repeatMode = if (repeat == -1) ObjectAnimator.RESTART else ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()

            setObjectValues(*quaternions)  // TODO update
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(QuaternionEvaluator())
        }
        return objectAnimator
    }

    private fun createIdleAnimator(duration: Long, repeat: Int, vararg quaternions: Quaternion): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()
        objectAnimator.apply {
            target = this@BugAnimationNode
            this.duration = duration
            propertyName = "localRotation"  // TODO update
            repeatCount = repeat
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()

            setObjectValues(*quaternions)  // TODO update
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(QuaternionEvaluator())
        }
        return objectAnimator
    }

    private fun createRandomXYZAnimator(duration: Long, repeat: Int, vararg vectors: Vector3): ObjectAnimator {
        return ObjectAnimator().apply {
            target = this@BugAnimationNode
            this.duration = duration
            propertyName = "localPosition"
            repeatCount = repeat // ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()

            setObjectValues(*vectors)
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
    }

    fun randomFloat(from: Float, to: Float): Float {
        return Random().nextFloat() * (to - from) + from
    }

    // --- animate non stop --- //
    fun animateNoneStop() {
        // when animation finish, animate something else, with delay

    }

    // --- transitional animation --- //
    fun animateRandomXYZ(duration: Long = 3000L, repeat: Int = 0) {
        val first = Vector3(localPosition)
        val transition1 = Vector3(localPosition).apply {
            x += randomFloat(-0.5F, 0.5F)
            y += randomFloat(-0.5F, 0.5F)
            z += randomFloat(-0.5F, 0.5F)
        }
        val transition2 = Vector3(localPosition).apply {
            x += randomFloat(-0.7F, 0.4F)
            y += randomFloat(-0.7F, 0.4F)
            z += randomFloat(-1.0F, 0.5F)
        }
        val transition3 = Vector3(localPosition).apply {
            x += randomFloat(-0.7F, 0.4F)
            y += randomFloat(-0.7F, 0.4F)
            z += randomFloat(-1.0F, 0.5F)
        }

        val animator = createRandomXYZAnimator(duration, repeat, first, transition1, transition2, transition3)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }

    fun animateFlyUpDown() {
        val start = Vector3(localPosition)
        val end = Vector3(localPosition).apply { y = +.3F }
        val animator = createFlyAnimator(ANIMATION_DURATION, 5, start, end)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }

    fun animateFlyFrontBack() {
        val first = Vector3(localPosition)
        val second = Vector3(localPosition).apply { z -= 0.5F }
        val third = Vector3(localPosition).apply { z += 0.7F }
        val fourth = Vector3(localPosition).apply { z -= 0.2F }
        val animator = createFlyAnimator(3000, 5, first, second, third, fourth)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }

    // --- rotational animation --- //
    fun animateInfiniteIdle(activity: Activity, rotationOffset: Float = 180F, duration: Long = 1000) {
        val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0 + rotationOffset)
        val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 30 + rotationOffset)
        val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0 + rotationOffset)
        val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), -30 + rotationOffset)
        val orientation5 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0 + rotationOffset)
        var orbitAnimation = createIdleAnimator(duration, 3, orientation1, orientation2, orientation3, orientation4, orientation5)

        orbitAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                Handler().postDelayed({ animateInfiniteIdle(activity, randomFloat(0f, 360f))}, 3000)
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        orbitAnimation.start()

    }

    fun animateIdle(duration: Long = 500, repeat: Int = 3) {
        val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
        val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 210f)
        val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
        val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 150f)
        val orientation5 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)

        var orbitAnimation = createIdleAnimator(duration, repeat, orientation1, orientation2, orientation3, orientation4, orientation5)
        orbitAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        orbitAnimation.start()
    }

    fun animateRotate() {
        val animator = createRotateAnimator(
                ROTATION_DURATION,
                0,
                Quaternion(Vector3(0.0F, 1.0F, 0.0F), 180F),
                Quaternion(Vector3(0.0F, 1.0F, 0.0F), 0F),
                Quaternion(Vector3(0.0F, 1.0F, 0.0F), -180F),
                Quaternion(Vector3(0.0F, 1.0F, 0.0F), 0F),
                Quaternion(Vector3(0.0F, 1.0F, 0.0F), 180F)
        )
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }

    fun animateRotateCircle() {
        val animator = createRotateAnimator(
            12000L,
            -1,
            Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f),
            Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f),
            Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f),
            Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)
        )
        animator.start()
    }

    // --- boss movement --- //
    fun bossAnimateUp(activity: Activity, duration: Long = 3000) {
        val start = Vector3(localPosition)
        val end = Vector3(localPosition).apply { y += 0.6F }
        val animator = createFlyAnimator(duration, 0, start, end)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                bossAnimateInfinitXYZ(activity, 3000 * SLOW_RATIO)
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }

    fun bossAnimateInfinitXYZ(activity: Activity, duration: Long = 1000) {
        val first = Vector3(localPosition)
        val negRange = -0.4F
        val posRange = 0.4F
        val transition1 = Vector3(localPosition).apply {
            x += randomFloat(negRange, posRange)
            y += randomFloat(negRange, posRange)
            z += randomFloat(negRange, posRange)
        }
        val transition2 = Vector3(localPosition).apply {
            x += randomFloat(negRange, posRange)
            y += randomFloat(negRange, posRange)
            z += randomFloat(negRange, posRange)
        }
        val transition3 = Vector3(localPosition).apply {
            x += randomFloat(negRange, posRange)
            y += randomFloat(negRange, posRange)
            z += randomFloat(negRange, posRange)
        }

        val animator = createRandomXYZAnimator(duration, 0, first, transition1, transition2, transition3)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                bossAnimateInfinitXYZ(activity, 4000 * SLOW_RATIO)
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }
//    private fun startAnimation() {
//        if (animator != null) return
//        low = Vector3(localPosition)
//        high = Vector3(localPosition).apply { y = +.4F }
//        animator = createAnimator()
//        animator?.start()
//    }
//
//    private fun stopAnimation() {
//        animator?.cancel()
//        animator = null
//    }
}