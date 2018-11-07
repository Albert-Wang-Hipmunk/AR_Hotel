package com.example.mooqoo.myapplication.Node

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

class BugAnimationNode : Node() {
    private val ANIMATION_DURATION = 500L
    private val ROTATION_DURATION = 2000L

//    var animator: ObjectAnimator? = null
    // TODO animation state
    var movementState: MOVEMENT = MOVEMENT.JUMP

    private fun createFlyAnimator(vararg vectors: Vector3): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()

        objectAnimator.apply {
            target = this@BugAnimationNode
            duration = ANIMATION_DURATION
            propertyName = "localPosition"
            repeatCount = 5 // ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()

            setObjectValues(*vectors)
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
        return objectAnimator
    }

    private fun createRotateAnimator(vararg quaternions: Quaternion): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()
        objectAnimator.apply {
            target = this@BugAnimationNode
            duration = ROTATION_DURATION
            propertyName = "localRotation"  // TODO update
            repeatCount = 0
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()

            setObjectValues(*quaternions)  // TODO update
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(QuaternionEvaluator())
        }
        return objectAnimator
    }

    fun animateFlyUpDown() {
        val start = Vector3(localPosition)
        val end = Vector3(localPosition).apply { y = +.3F }
        val animator = createFlyAnimator(start, end)
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
        val animator = createFlyAnimator(first, second, third, fourth)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}
        })
        animator.start()
    }

    fun animateRotate() {
        val animator = createRotateAnimator(
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