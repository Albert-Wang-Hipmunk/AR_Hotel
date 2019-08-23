package com.example.mooqoo.myapplication.JT_Module

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.example.mooqoo.myapplication.PointerDrawable
import com.example.mooqoo.myapplication.R
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
import kotlinx.android.synthetic.main.activity_game.view_content
import kotlinx.android.synthetic.main.activity_jt_game.*
import java.lang.Exception
import java.util.concurrent.CompletableFuture

class JTGameActivity : AppCompatActivity() {

    lateinit var fragment: ArFragment

    lateinit var treasureRenderable: ModelRenderable
    lateinit var bossRenderable: ModelRenderable
    lateinit var jtImageViewRenderable: ViewRenderable

    var anchorNode: AnchorNode? = null

    var hitcount = 0
    var bossLife = 10

//    lateinit var bossNode: BugAnimationNode
//    lateinit var bossCardNode: BugAnimationNode

    val pointer = PointerDrawable()
    private var isTracking = false
    private var isHitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jt_game)

        fragment = getSupportFragmentManager().findFragmentById(R.id.sceneform_jt_game_fragment) as ArFragment
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
            onUpdate()
        }

        createGameRenderable()

        setupBtnClick()

//        btn_start_game.setOnClickListener { startGame() }
    }

    private fun setupBtnClick() {
        btn_1.setOnClickListener { addJTToPlane() }
        btn_2.setOnClickListener {  }
        btn_3.setOnClickListener {  }
    }

    private fun addJTToPlane() {
        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    val jtNode = addNodeToScene(fragment, hit.createAnchor(), jtImageViewRenderable)
                    jtNode.setOnTouchListener { hitTestResult, motionEvent -> handleTouchBoss(hitTestResult, motionEvent) }
                    jtNode.animateInfiniteIdle(this, 0F)
                    jtNode.bossAnimateUp(this, 5000L)
                }
            }
        }
    }

    private fun startGame() {
        anchorNode?.setParent(null)
        addBossToPlane()
        hitcount = 0
        btn_start_game.visibility = View.GONE
    }

    private fun createGameRenderable() {
        val treasureFutureRenderable = ModelRenderable.builder().setSource(fragment.context, Uri.parse("treasure.sfb")).build()
        val bossFutureRenderable = ModelRenderable.builder().setSource(fragment.context, Uri.parse("boss.sfb")).build()
        val jtFutureRenderable = ViewRenderable.builder().setView(this, R.layout.jt_imageview).build()

        CompletableFuture.allOf(
                treasureFutureRenderable,
                bossFutureRenderable,
                jtFutureRenderable
        ).handle { _, throwable ->
            if (throwable != null) {
                Toast.makeText(this, "Unable to load renderabl", Toast.LENGTH_SHORT).show()
                return@handle
            }

            try {
                treasureRenderable = treasureFutureRenderable.get()
                bossRenderable = bossFutureRenderable.get()
                jtImageViewRenderable = jtFutureRenderable.get()
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

    private fun addNodeToParent(
            renderable: Renderable,
            parentNode: Node,
            offset: Vector3 = Vector3(0.2f, 0.2f, 0.2f),
            scale: Vector3? = null
    ): BugAnimationNode {
        val node = BugAnimationNode()
        node.renderable = renderable
        if (scale != null) node.localScale = scale
        node.localPosition = offset
        node.setParent(parentNode)
        return node
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable): BugAnimationNode {
        anchorNode = AnchorNode(anchor)
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
                    val bossCardNode = addNodeToParent(jtImageViewRenderable, bossNode, Vector3(0.0f, 2.0f, 0.0f))
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

                //
                // TODO change color
                hitcount++
                if (hitcount >= bossLife) {
                    node.localPosition
                    val treasureNode = addNodeToParent(treasureRenderable, anchorNode as Node, node.localPosition, Vector3(0.5f, 0.5f, 0.5f))
                    treasureNode.animateRotateCircle()
                    node.setParent(null)
                    gameEnd()
                }

                // update hp
                jtImageViewRenderable.view.findViewById<View>(R.id.health_hp).layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (bossLife - hitcount).toFloat())
                jtImageViewRenderable.view.findViewById<View>(R.id.health_empty).layoutParams =
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
