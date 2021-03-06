package com.example.mooqoo.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.mooqoo.myapplication.Node.AnimationNode
import com.example.mooqoo.myapplication.Node.BugAnimationNode
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_augmented_image.*
import java.lang.Exception
import java.util.HashMap
import android.content.Intent
import android.net.Uri
import android.widget.Button


class AugmentedImageActivity : AppCompatActivity() {
    lateinit var arFragment: ArFragment

    private lateinit var scrollViewRenderable: ViewRenderable
    private val hipmunkRenerableList: MutableList<ViewRenderable?> = MutableList(5) { _ -> null}
    private val augmentedImageMap: MutableMap<AugmentedImage, Node> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmented_image)

        arFragment = augmented_image_fragment as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener { onUpdateFrame(it) }

        // TODO
        createHipmunkCard(0, "Phoenix Hotel, SF", 79.00, R.drawable.hotel_2)
        createHipmunkCard(1, "San Francisco Marriott Marquis", 289.00, R.drawable.hotel_6)
        createHipmunkCard(2, "Hotel Beresford, SF", 124.00, R.drawable.hotel_3)
        createHipmunkCard(3, "Hotel Deva, SF", 68.00, R.drawable.hotel_4)
        createHipmunkCard(4, "Warfield Hotel, SF", 110.00, R.drawable.hotel_5)
        createScrollHipmunkCard()
    }

    fun createScrollHipmunkCard() {
        ViewRenderable.builder().setView(this, R.layout.scroll_hipmunk_card).build().thenAccept {
            scrollViewRenderable = it
            updateHipmunkScrollView(R.id.card_1, "Hotel Drisco, SF ", 139.00, R.drawable.hotel_1)
            updateHipmunkScrollView(R.id.card_2, "San Francisco Marriott Marquis", 289.00, R.drawable.hotel_6)
//            updateHipmunkScrollView(R.id.card_2, "Phoenix Hotel, SF", 79.00, R.drawable.hotel_2)
            updateHipmunkScrollView(R.id.card_3, "Hotel Beresford, SF", 124.00, R.drawable.hotel_3)
            updateHipmunkScrollView(R.id.card_4, "Hotel Deva, SF", 68.00, R.drawable.hotel_4)
            updateHipmunkScrollView(R.id.card_5, "Warfield Hotel, SF", 110.00, R.drawable.hotel_5)
        }
    }

    private fun updateHipmunkScrollView(viewId: Int, hotelName: String, hotelPrice: Double, hotelImgId: Int) {
        scrollViewRenderable.view.findViewById<View>(viewId).apply {
            setOnClickListener { onHipmunkCardClicked() }
            findViewById<TextView>(R.id.tv_hotel_name).text = hotelName
            findViewById<TextView>(R.id.tv_hotel_price).text = "$$hotelPrice"
            findViewById<ImageView>(R.id.iv_hotel_src).setImageResource(hotelImgId)
        }
    }

    fun createHipmunkCard(index: Int, hotelName: String, hotelPrice: Double, hotelImgId: Int) {
        ViewRenderable.builder()
            .setView(this, R.layout.hipmunk_card)
            .build()
            .thenAccept {
                hipmunkRenerableList[index] = it
                it.view.apply {
                    setOnClickListener { onHipmunkCardClicked() }
                    findViewById<TextView>(R.id.tv_hotel_name).text = hotelName
                    findViewById<TextView>(R.id.tv_hotel_price).text = "$$hotelPrice"
                    findViewById<ImageView>(R.id.iv_hotel_src).setImageResource(hotelImgId)
                }
//                addNodeToScene(arFragment, augmentedImage, it)
            }
    }

    fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame
        val updatedAugmentedImages = frame?.getUpdatedTrackables(AugmentedImage::class.java)


        updatedAugmentedImages?.forEach { augmentedImage ->
            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> {
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    val text = "Detected Image " + augmentedImage.index
                    Log.d("TESTT", "TrackingState.PAUSED: $text")
//                    SnackbarHelper.getInstance().showMessage(this, text)
                }

                TrackingState.TRACKING -> {
                    // Have to switch to UI Thread to update View.
//                    fitToScanView.setVisibility(View.GONE)
                    Log.d("TESTT", "TrackingState.TRACKING------------")
                    // Create a new anchor for newly found images.
                    when (augmentedImage.index) {
                        0 -> {
                            if (!augmentedImageMap.containsKey(augmentedImage)) {
//                                Toast.makeText(this, "hipmunk image found!, index=${augmentedImage.index}", Toast.LENGTH_SHORT).show()
//                                placeMultipleObject(augmentedImage)
                                placeScrollCard(augmentedImage)
//                                placeObject(augmentedImage, 0)
                            }

//                            Toast.makeText(this, "hipmunk image 1 found!", Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
//                            Toast.makeText(this, "hipmunk image 2 found!", Toast.LENGTH_SHORT).show()
                            if (!augmentedImageMap.containsKey(augmentedImage)) {
                                placeObject(augmentedImage, 1)
                            }
                        }
                        2 -> {
//                            Toast.makeText(this, "hipmunk image 3 found!", Toast.LENGTH_SHORT).show()
                            if (!augmentedImageMap.containsKey(augmentedImage)) {
                                placeObject(augmentedImage, 2)
                            }
                        }
                        else -> {}
                    }


                }

                TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage)
            }
        }
    }

    private fun placeScrollCard(augmentedImage: AugmentedImage) {
        addNodeToScene(arFragment, augmentedImage, scrollViewRenderable)
    }
    private fun placeMultipleObject(augmentedImage: AugmentedImage) {
        addNodeToScene(arFragment, augmentedImage, hipmunkRenerableList[0], Vector3(0.3f, 0f, 0.2f))?.animateIdle()//?.animateOrbit()
        addNodeToScene(arFragment, augmentedImage, hipmunkRenerableList[1])//?.animateOrbit()
        addNodeToScene(arFragment, augmentedImage, hipmunkRenerableList[2], Vector3(-0.3f, 0f, 0.2f))?.animateIdle(3000L)//?.animateOrbit()
    }

    private fun placeObject(augmentedImage: AugmentedImage, index: Int) {
        addNodeToScene(arFragment, augmentedImage, hipmunkRenerableList[index])?.animateIdle(2000L, 2)
    }

    // TODO add deeplink
    private fun onHipmunkCardClicked() {
//        Toast.makeText(this, "hipmunk card is clicked!", Toast.LENGTH_SHORT).show()

//        val intent = packageManager.getLaunchIntentForPackage("com.hipmunk.android")
//        startActivity(intent)
        val url2 = "http://hipmunk.com/hotel/5257f8457f2c6c2dd900020c?w=San+Francisco,+CA;i=2018-11-16;o=2018-11-18;is_search_for_business=false"
        val url = "http://hipmunk.com/hotel/509bfdce66edc8466101945e?w=San+Francisco,+CA;i=2018-11-16;o=2018-11-18;is_search_for_business=false"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url2)
        startActivity(i)
    }

    private fun addNodeToScene(
            fragment: ArFragment,
            augmentedImage: AugmentedImage,
            renderable: Renderable?,
            offsetVector: Vector3 = Vector3(0f, 0f, 0.2f)
    ): BugAnimationNode? {
        if (renderable == null) {
            Toast.makeText(this, "renderable is null", Toast.LENGTH_SHORT).show()
            return null
        }

        var pos = augmentedImage.centerPose
        var rotArray = pos.rotationQuaternion
        rotArray[0] = 0.0f
        rotArray[2] = 0.0f
        val newPos = Pose(pos.translation, rotArray)

        val anchorNode = AnchorNode(augmentedImage.createAnchor(newPos))
        fragment.arSceneView.scene.addChild(anchorNode)
        val node = BugAnimationNode()// AnimationNode(fragment.transformationSystem)
        node.setParent(anchorNode)
        node.renderable = renderable
        node.worldScale = Vector3(0.2f, 0.2f, 0.2f)
        node.localPosition = offsetVector
        augmentedImageMap.put(augmentedImage, node)
        return node
//        node.select()
    }

    private fun addAnchorNode(augmentedImage: AugmentedImage) {
        val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
    }
}
