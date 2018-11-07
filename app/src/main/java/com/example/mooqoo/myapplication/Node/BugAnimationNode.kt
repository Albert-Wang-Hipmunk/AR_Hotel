package com.example.mooqoo.myapplication.Node

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

class BugAnimationNode : Node() {
    private val ANIMATION_DURATION = 500L

//    var animator: ObjectAnimator? = null
    // TODO animation state
    var movementState: MOVEMENT = MOVEMENT.JUMP

    // fly up and down
    private fun createFlyAnimator(start: Vector3, end: Vector3): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()

        objectAnimator.apply {
            target = this@BugAnimationNode
            duration = ANIMATION_DURATION
            propertyName = "localPosition"
            repeatCount = 5 // ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()

            setObjectValues(start, end)
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
        return objectAnimator
    }

    private fun createRotateAnimator(): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()

        objectAnimator.apply {
            target = this@BugAnimationNode
            duration = ANIMATION_DURATION
            propertyName = "localPosition"  // TODO update
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()

//            setObjectValues(low, high)  // TODO update
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
        return objectAnimator
    }

    fun animateFly() {
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

    fun animateRotate() {
        val animator = createRotateAnimator()
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