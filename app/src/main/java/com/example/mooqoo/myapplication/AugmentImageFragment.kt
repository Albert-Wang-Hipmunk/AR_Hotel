package com.example.mooqoo.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.lang.Exception

class AugmentedImageFragment : ArFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)
        if (!setupAugmentedImageDatabase(config, session)) {
            Toast.makeText(context, "Could not setup augmented image database", Toast.LENGTH_LONG).show()
        }
        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
        try {
            val inputStream = context!!.assets.open("hipmunk.imgdb")
            val imageDatabase = AugmentedImageDatabase.deserialize(session, inputStream)

            config.augmentedImageDatabase = imageDatabase
            session.configure(config)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}