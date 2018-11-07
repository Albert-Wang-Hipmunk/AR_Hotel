package com.example.mooqoo.myapplication.Node

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

enum class MOVEMENT { FRONT, BACK, RIGHT, LEFT, PAUSE, JUMP }

//class AnimationNode : Node() {
class AnimationNode(transformationSystem: TransformationSystem) : TransformableNode(transformationSystem) {

    var movementAnimation: ObjectAnimator? = null
    var movementState: MOVEMENT = MOVEMENT.JUMP

    private val ANIMATION_DURATION = 3000L

    lateinit var low: Vector3
    lateinit var high: Vector3

    override fun onUpdate(frameTime: FrameTime) {
        super.onUpdate(frameTime)

        // do nothing
        if (movementAnimation == null) return

        when(movementState) {
            MOVEMENT.PAUSE -> { movementAnimation?.pause() }
            else -> {
                movementAnimation?.apply {
                    resume()
                    duration = ANIMATION_DURATION
                    setCurrentFraction(animatedFraction)
                }

            }
        }

    }

    // TODO create animator
    private fun createAnimator(): ObjectAnimator {
        val objectAnimator: ObjectAnimator = ObjectAnimator()

        objectAnimator.apply {
            target = this@AnimationNode
            duration = ANIMATION_DURATION
            propertyName = "localPosition"
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()

            setObjectValues(low, high)
            setAutoCancel(true)

            // Always apply evaluator AFTER object values or it will be overwritten by a default one
            setEvaluator(VectorEvaluator())
        }
        return objectAnimator
    }

    private fun startAnimation() {
        if (movementAnimation != null) return
        low = Vector3(localPosition)
        high = Vector3(localPosition).apply { y = +.4F }
        movementAnimation = createAnimator()
        movementAnimation?.start()
    }

    private fun stopAnimation() {
        movementAnimation?.cancel()
        movementAnimation = null
    }

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }


}