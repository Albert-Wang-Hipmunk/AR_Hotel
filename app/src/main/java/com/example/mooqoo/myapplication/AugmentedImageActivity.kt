package com.example.mooqoo.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mooqoo.myapplication.Node.AnimationNode
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_augmented_image.*
import java.lang.Exception
import java.util.HashMap


class AugmentedImageActivity : AppCompatActivity() {
    lateinit var arFragment: ArFragment
    private val augmentedImageMap: MutableMap<AugmentedImage, Node> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmented_image)

        arFragment = augmented_image_fragment as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener { onUpdateFrame(it) }
    }

    fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)


        updatedAugmentedImages.forEach { augmentedImage ->
            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> {
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    val text = "Detected Image " + augmentedImage.index
//                    SnackbarHelper.getInstance().showMessage(this, text)
                }

                TrackingState.TRACKING -> {
                    // Have to switch to UI Thread to update View.
//                    fitToScanView.setVisibility(View.GONE)

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        Toast.makeText(this, "hipmunk image found!", Toast.LENGTH_SHORT).show()
                        placeObject(augmentedImage)
                    }
                }

                TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage)
            }
        }
    }

    private fun placeObject(augmentedImage: AugmentedImage) {
        ViewRenderable.builder()
                .setView(this, R.layout.hipmunk_card)
                .build()
                .thenAccept { addNodeToScene(arFragment, augmentedImage, it) }
    }

    private fun addNodeToScene(fragment: ArFragment, augmentedImage: AugmentedImage, renderable: Renderable) {
        val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
        val node = BugAnimationNode()// AnimationNode(fragment.transformationSystem)
        node.renderable = renderable
        node.worldScale = Vector3(0.3f, 0.3f, 0.3f)
//        node.localScale = Vector3(0.05f, 0.05f, 0.05f)
        node.setParent(anchorNode)
        node.animateRotate()
        fragment.arSceneView.scene.addChild(anchorNode)
        augmentedImageMap.put(augmentedImage, node)
//        node.select()
    }

    private fun addAnchorNode(augmentedImage: AugmentedImage) {
        val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
    }
}
