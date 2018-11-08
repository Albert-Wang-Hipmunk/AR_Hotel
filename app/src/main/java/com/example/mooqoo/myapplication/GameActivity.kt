package com.example.mooqoo.myapplication

import android.graphics.Point
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.example.mooqoo.myapplication.Node.AnimationNode
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    lateinit var fragment: ArFragment

    val pointer = PointerDrawable()
    private var isTracking = false
    private var isHitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        fragment = getSupportFragmentManager().findFragmentById(R.id.sceneform_game_fragment) as ArFragment
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
            onUpdate()
        }

        btn_start_game.setOnClickListener { startGame() }
    }

    private fun startGame() {
        addObject(Uri.parse("boss.sfb"))
        btn_start_game.visibility = View.GONE
    }

    /**
     * updateTracking()
     * uses ARCore's camera state and returns true if the tracking state has changed since last call.
     */
    private fun updateTracking(): Boolean {
        val frame = fragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame != null && frame.camera.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    private fun getScreenCenter(): Point {
        return Point(view_content.width/2,  view_content.height/2)
    }

    /**
     * updateHitTest()
     * also uses ARCore to call Frame.hitTest(). As soon as any hit is detected, the method returns.
     */
    private fun updateHitTest(): Boolean {
        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        var hits = emptyList<HitResult>()
        val wasHitting = isHitting
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            hits.forEach { hit ->
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    return@forEach
                }
            }
        }

        return wasHitting != isHitting
    }

    private fun onUpdate() {
        if (view_content == null) return
        val trackingChanged = updateTracking()
        if (trackingChanged) {
            if (isTracking) view_content.overlay.add(pointer)
            else view_content.overlay.remove(pointer)
            view_content.invalidate()
        }

        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                pointer.enabled = isHitting
                view_content.invalidate()
            }
        }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = BugAnimationNode()
        node.renderable = renderable
        node.localScale = Vector3(0.2f, 0.2f, 0.2f)
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)

//        node.select()
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        val renderableFuture = ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
                .exceptionally { throwable ->
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(throwable.message)
                            .setTitle("Codelab error!")
                    val dialog = builder.create()
                    dialog.show()
                    null
                }
    }

    private fun addObject(model: Uri) {
        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(fragment, hit.createAnchor(), model)
                    break

                }
            }
        }
    }
}
