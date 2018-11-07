package com.example.mooqoo.myapplication

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    lateinit var scene: Scene
    lateinit var bugNode: Node

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        setSupportActionBar(toolbar)

        scene = scene_view.scene
        renderObject(Uri.parse("bug.sfb"))
//        val camera = scene_view.scene.camera
//        camera.localRotation = Quaternion.axisAngle(Vector3.right(), -30.0f)
    }

    private fun addNodeToScene(model: ModelRenderable) {
        model?.let {
            bugNode = Node().apply {
                setParent(scene)
                localPosition = Vector3(0f, 0f, -1f)
                localRotation = Quaternion(0f, 0.5f, 0f, 0f)
                localScale = Vector3(0.5f, 0.5f, 0.5f)
                name = "Bug"
                renderable = it
            }
            scene.addChild(bugNode)
        }
    }

    private fun renderObject(modelUri: Uri) {
        ModelRenderable.builder()
                .setSource(this, modelUri)
                .build()
                .thenAccept {
                    addNodeToScene(it)
                }
                .exceptionally {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    return@exceptionally null
                }

    }

    override fun onPause() {
        super.onPause()
        scene_view.pause()
    }

    override fun onResume() {
        super.onResume()
        scene_view.resume()
    }


//    private fun addObject(model: Uri) {
//        val scene = scene_view.scene
//        // TODO create motion event
//        val hitTestResult: HitTestResult = scene.hitTest(object : MotionEvent() {
//
//        })
//
//        hitTestResult.node
//
//        val pt = getScreenCenter()
//        val hits: List<HitResult>
//        if (frame != null) {
//            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
//            for (hit in hits) {
//                val trackable = hit.trackable
//                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
//                    placeObject(fragment, hit.createAnchor(), model)
//                    break
//
//                }
//            }
//        }
//    }
}