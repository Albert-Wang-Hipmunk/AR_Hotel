package com.example.mooqoo.myapplication.JT_Module

import android.content.ContentValues
import android.graphics.Point
import android.media.CamcorderProfile
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.mooqoo.myapplication.JT_Module.photo_gallery.PhotoAdapter
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.example.mooqoo.myapplication.R
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.samples.videorecording.WritingArFragment
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_jt_game.*
import java.lang.Exception
import java.util.concurrent.CompletableFuture

class JTGameActivity : AppCompatActivity() {
    val TAG = "JTGameActivity"
    lateinit var fragment: WritingArFragment

    val photoResources = listOf(
            R.drawable.jt_iv_1, R.drawable.jt_iv_2, R.drawable.jt_iv_3,
            R.drawable.jt_iv_1, R.drawable.jt_iv_2, R.drawable.jt_iv_3,
            R.drawable.jt_iv_1, R.drawable.jt_iv_2, R.drawable.jt_iv_3
    )
    val imageViewRenderables = mutableListOf<ViewRenderable>()

    var anchorNode: AnchorNode? = null

    val pointer = PointerDrawable()
    private var isTracking = false
    private var isHitting = false

    private var videoRecorder: VideoRecorder? = null

    interface PhotoClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jt_game)

        fragment = getSupportFragmentManager().findFragmentById(R.id.sceneform_jt_game_fragment) as WritingArFragment
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
            onUpdate()
        }

        createGameRenderable()

        initPhotoGallery()
        initRecorder()
        setupBtnClick()
    }

    private fun initPhotoGallery() {
        val photoClickListener: PhotoClickListener = object : PhotoClickListener {
            override fun onItemClick(view: View, position: Int) {
                // TODO
                addDomToPlane(imageViewRenderables[position])
            }
        }
        rv_photo_gallery.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_photo_gallery.adapter = PhotoAdapter(photoResources, this, photoClickListener)
    }

    private fun initRecorder() {
        // Initialize the VideoRecorder
        videoRecorder = VideoRecorder()
        val orientation = resources.configuration.orientation
        videoRecorder?.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)
        videoRecorder?.setSceneView(fragment.arSceneView)
    }

    private fun setupBtnClick() {
        btn_1.setOnClickListener { addDomToPlane(imageViewRenderables[0]) }
        btn_record.setOnClickListener { toggleRecording() }
        btn_3.setOnClickListener { recreate() }
    }

    private fun toggleRecording() {
        if (!fragment.hasWritePermission()) {
            Log.e(TAG, "Video recording requires the WRITE_EXTERNAL_STORAGE permission")
            Toast.makeText(
                    this,
                    "Video recording requires the WRITE_EXTERNAL_STORAGE permission",
                    Toast.LENGTH_LONG)
                    .show()
            fragment.launchPermissionSettings()
            return
        }
        val recording = videoRecorder?.onToggleRecord() ?: false
        if (recording) {
            btn_record.setImageResource(R.drawable.round_stop)
        } else {
            btn_record.setImageResource(R.drawable.round_videocam)
            val videoPath = videoRecorder?.videoPath?.absolutePath ?: ""
            if (videoPath.isEmpty()) Toast.makeText(this, "VideoPath is empty", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "Video saved: $videoPath", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Video saved: $videoPath")

            // Send  notification of updated content.
            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATA, videoPath)
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun addDomToPlane(viewRenderable: ViewRenderable?) {
        if (viewRenderable == null) return

        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    val scaleAmount = randomNumScaleTo(0.3)
                    val randomScale = Vector3(scaleAmount, scaleAmount, scaleAmount)
                    val jtNode = addNodeToScene(fragment, hit.createAnchor(), viewRenderable, randomScale)
                    jtNode.animateInfiniteIdle(this, 0F)
                    jtNode.bossAnimateUp(this, 5000L)
                }
            }
        }
    }

    private fun randomNumScaleTo(scale: Double = 1.0): Float =  (Math.random() * scale).toFloat()

    private fun createGameRenderable() {
        val futureRenderables = mutableListOf<CompletableFuture<ViewRenderable>>()
        photoResources.forEach {
            val imageView = ImageView(this)
            imageView.setImageResource(it)
            futureRenderables.add(ViewRenderable.builder().setView(this, imageView).build())
        }


        CompletableFuture.allOf(*futureRenderables.toTypedArray()).handle { _, throwable ->
            if (throwable != null) {
                Toast.makeText(this, "Unable to load futureRenderable", Toast.LENGTH_SHORT).show()
                return@handle
            }
            try {
                futureRenderables.forEach { imageViewRenderables.add(it.get()) }
            } catch (e: Exception) {
                Toast.makeText(this, "futureRenderables create failed: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable, scale: Vector3 = Vector3(0.3f, 0.3f, 0.3f)): BugAnimationNode {
        anchorNode = AnchorNode(anchor)
        val node = BugAnimationNode()
        node.renderable = renderable
        node.localScale = scale // Vector3(0.3f, 0.3f, 0.3f)
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        return node
    }

}
