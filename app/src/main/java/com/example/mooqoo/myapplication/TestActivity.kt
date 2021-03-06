package com.example.mooqoo.myapplication

import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Toast
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.Camera
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
    lateinit var camera: Camera

    lateinit var bugNode: BugAnimationNode


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        setSupportActionBar(toolbar)

        scene = scene_view.scene
        renderObject(Uri.parse("bug.sfb"))

        setupBtn()

        camera = scene_view.scene.camera
//        camera.localRotation = Quaternion(Vector3(0f, 1f, 0f), 180f)
//        camera.localRotation = Quaternion.axisAngle(Vector3.right(), -30.0f)
    }

    private fun setupBtn() {
        btn_toggle_main.setOnClickListener { startMainActivity() }
        btn_toggle_aug.setOnClickListener { startAugActivity() }
        btn_toggle_game.setOnClickListener { startGameActivity() }

        btn_stand_pose.setOnClickListener { bugNode.animateIdle() }
        btn_random_translate.setOnClickListener { bugNode.animateRandomXYZ() }
        btn_other.setOnClickListener { bugNode.animateInfiniteIdle(this) }
        btn_up_down.setOnClickListener { bugNode.animateFlyUpDown() }
        btn_rotate.setOnClickListener { bugNode.animateRotate() }
        btn_front_back.setOnClickListener { bugNode.animateFlyFrontBack() }
    }

    private fun startGameActivity() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startAugActivity() {
        val intent = Intent(this, AugmentedImageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun addNodeToScene(model: ModelRenderable) {
        model?.let {
            bugNode = BugAnimationNode().apply {
                setParent(scene)
                localPosition = Vector3(0f, -0.5f, -1.5f)
//                localRotation = Quaternion(0f, 0.0f, 0f, 0f)
                localRotation = Quaternion(Vector3(0f, 1f, 0f), 180f)
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

}