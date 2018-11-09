package com.example.mooqoo.myapplication

import android.app.ActionBar
import android.graphics.Point
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_game.*
import java.lang.Exception
import java.util.concurrent.CompletableFuture

class GameActivity : AppCompatActivity() {

    lateinit var fragment: ArFragment

    lateinit var bossRenderable: ModelRenderable
    lateinit var bossCardViewRenderable: ViewRenderable

    var hitcount = 0
    var bossLife = 10

//    lateinit var bossNode: BugAnimationNode
//    lateinit var bossCardNode: BugAnimationNode

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

        createBossRenderable()
        btn_start_game.setOnClickListener { startGame() }
    }

    private fun startGame() {
        addBossToPlane()
        hitcount = 0
        btn_start_game.visibility = View.GONE
    }

    private fun createBossRenderable() {
        val bossFutureRenderable = ModelRenderable.builder().setSource(fragment.context, Uri.parse("boss.sfb")).build()
        val bossCardFutureRenderable = ViewRenderable.builder().setView(this, R.layout.boss_card).build()

        CompletableFuture.allOf(
            bossFutureRenderable,
            bossCardFutureRenderable
        ).handle { _, throwable ->
            if (throwable != null) {
                Toast.makeText(this, "Unable to load renderabl", Toast.LENGTH_SHORT).show()
                return@handle
            }

            try {
                bossRenderable = bossFutureRenderable.get()
                bossCardViewRenderable = bossCardFutureRenderable.get()
            } catch (e: Exception) {
                Toast.makeText(this, "createBossNode: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun addNode(renderable: Renderable, parentNode: Node, offset: Vector3 = Vector3(0.2f, 0.2f, 0.2f)): BugAnimationNode {
        val node = BugAnimationNode()
        node.renderable = renderable
//        node.localScale = offset
        node.localPosition = offset
        node.setParent(parentNode)
//        node.animateRotateCircle()
        return node
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable): BugAnimationNode {
        val anchorNode = AnchorNode(anchor)
        val node = BugAnimationNode()
        node.renderable = renderable
        node.localScale = Vector3(0.3f, 0.3f, 0.3f)
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        return node
    }

    private fun addBossToPlane() {
        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    val bossNode = addNodeToScene(fragment, hit.createAnchor(), bossRenderable)
                    bossNode.setOnTouchListener { hitTestResult, motionEvent -> handleTouchBoss(hitTestResult, motionEvent) }
                    bossNode.animateInfiniteIdle(this, 0F)
                    bossNode.bossAnimateUp(this, 5000L)
                    val bossCardNode = addNode(bossCardViewRenderable, bossNode, Vector3(0.0f, 2.0f, 0.0f))
                }
            }
        }
    }

    private fun handleTouchBoss(hitTestResult: HitTestResult, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action) {
//            MotionEvent.ACTION_BUTTON_RELEASE -> { Log.d("TESTT", "motion event = ACTION_BUTTON_RELEASE") }
            MotionEvent.ACTION_DOWN -> {
//                Log.d("TESTT", "motion event = ACTION_DOWN")
//                val node = hitTestResult.node
//                Log.d("TESTT", "node.name=${node.name}, node(parent)=${node.parent.name}")
            }
            MotionEvent.ACTION_UP -> {
                Log.d("TESTT", "motion event = ACTION_UP")
                val node = hitTestResult.node
                if (node == null) return true

                Log.d("TESTT", "update view... ... ...")

                // TODO change color
                hitcount++
                if (hitcount >= bossLife) {
                    node.setParent(null)
                    gameEnd()
                }

                // update hp
                bossCardViewRenderable.view.findViewById<View>(R.id.health_hp).layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (bossLife - hitcount).toFloat())
                bossCardViewRenderable.view.findViewById<View>(R.id.health_empty).layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, hitcount.toFloat())
            }
        }
        return true
    }

    private fun gameEnd() {
        btn_start_game.visibility = View.VISIBLE
        hitcount = 0
        bossLife += 5
    }
}
