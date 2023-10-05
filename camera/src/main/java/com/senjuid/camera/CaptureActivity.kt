package com.senjuid.camera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.android.synthetic.main.activity_capture.*

/**
 * Created by Hendi, 19 Sep 2019
 * */
class CaptureActivity : AppCompatActivity() {

    private lateinit var pageFinisher: PageFinisher
    private lateinit var helper: CaptureActivityHelper
    private lateinit var imageFileManager: ImageFileManager

    private val cameraListener = object : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            if (camera_view.flash == Flash.TORCH) {
                camera_view.flash = Flash.OFF
            }

            var maxSize = intent.getIntExtra("max_size", 0)
            helper.pictureResultHandler(result, maxSize) { bitmap ->
                // Check if the "disable_preview" extra in the intent is true
                if (intent.getBooleanExtra("disable_preview", false)) {
                    try {
                        helper.saveBitmapAndFinish(intent, camera_view.facing) {
                            val returnIntent = Intent()
                            returnIntent.putExtra("photo", it)
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    } catch (e: Exception) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("photo", "")
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                } else {
                    iv_preview.setImageBitmap(bitmap)
                    showProgressDialog(false)
                    viewMode(false)
                }
            }
        }
    }

    // MARK: Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        try {

            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            // Init file manager
            imageFileManager = ImageFileManager(this)
            imageFileManager.createDir()

            // Init helper
            helper = CaptureActivityHelper(imageFileManager)

            // Init page timer
            pageFinisher = PageFinisher(this, 3 * 1000 * 60)

            // Add camera listener
            camera_view.setLifecycleOwner(this)
            camera_view.addCameraListener(cameraListener)
            camera_view.setPictureSize(SizeSelectors.smallest())

            // Add take picture button listener
            btn_take_picture.setOnClickListener {
                var delay: Long = 0
                if (btn_flash_on.visibility == View.VISIBLE && camera_view.facing == Facing.BACK) {
                    camera_view.flash = Flash.TORCH
                    delay = 1000
                }
                showProgressDialog(true)
                Handler().postDelayed({
                    camera_view.playSounds = isAudioServiceMute()
                    val snapshot = intent.extras.getBoolean("is_snapshot", true)
                    if (snapshot) {
                        camera_view.takePictureSnapshot() // faster
                    } else {
                        camera_view.takePicture()
                    }
                }, delay)
            }

            // Add back button listener
            btn_back.setOnClickListener {
                finish()
            }

            // Add retake button listener
            btn_retake.setOnClickListener {
                viewMode(true)
            }

            // Add rotate button listener
            btn_rotate_picture.setOnClickListener {
                helper.rotate(iv_preview.rotation + 90) {
                    iv_preview.setImageBitmap(it)
                }
            }

            // Add select picture button listener
            btn_select_picture.setOnClickListener {
                try {
                    helper.saveBitmapAndFinish(intent, camera_view.facing) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("photo", it)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                } catch (e: Exception) {
                    val returnIntent = Intent()
                    returnIntent.putExtra("photo", "")
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }

            btn_flash_on.setOnClickListener {
                btn_flash_on.visibility = View.GONE
                btn_flash_off.visibility = View.VISIBLE
            }

            btn_flash_off.setOnClickListener {
                btn_flash_on.visibility = View.VISIBLE
                btn_flash_off.visibility = View.GONE
            }

            // set view mode
            viewMode(true)

            // Check if the "disable_back" extra in the intent is true
            if (intent.getBooleanExtra("disable_back", false)) {
                // Disable back camera functionality
                camera_view.facing = Facing.FRONT

                // Set visibility of face silhouette based on the "show_face_area" extra in the intent
                face_silhouette.visibility =
                    if (intent.getBooleanExtra("show_face_area", false)) View.VISIBLE else View.GONE
                btn_switch_camera.visibility = View.GONE
            } else {
                // Set camera facing based on the "facing_back" extra in the intent
                camera_view.facing =
                    if (intent.getBooleanExtra("facing_back", true)) Facing.BACK else Facing.FRONT

                // Set visibility of face silhouette based on the camera facing and "show_face_area" extra
                if (intent.getBooleanExtra(
                        "show_face_area",
                        false
                    ) && camera_view.facing == Facing.FRONT
                ) {
                    face_silhouette.visibility = View.VISIBLE
                } else {
                    face_silhouette.visibility = View.GONE
                }

                // Set visibility and click listener for the switch camera button
                btn_switch_camera.visibility = View.VISIBLE
                btn_switch_camera.setOnClickListener {
                    camera_view.toggleFacing()
                    if (intent.getBooleanExtra(
                            "show_face_area",
                            false
                        ) && camera_view.facing == Facing.FRONT
                    ) {
                        face_silhouette.visibility = View.VISIBLE
                    } else {
                        face_silhouette.visibility = View.GONE
                    }
                    camera_view.flash =
                        if (camera_view.facing == Facing.BACK) Flash.OFF else Flash.OFF
                    btn_flash_on.visibility =
                        if (camera_view.facing == Facing.BACK) View.GONE else View.GONE
                    btn_flash_off.visibility =
                        if (camera_view.facing == Facing.BACK) View.VISIBLE else View.GONE
                }
            }

        } catch (ex: Exception) {
            val data = Intent().apply {
                putExtra("native", true)
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        pageFinisher.start()
    }

    override fun onStop() {
        pageFinisher.cancel()
        super.onStop()
    }

    private fun showProgressDialog(show: Boolean) {
        layout_progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun viewMode(isCapture: Boolean) {
        if (isCapture) {
            layout_preview.visibility = View.GONE
        } else {
            layout_preview.visibility = View.VISIBLE
        }
    }
}

// Page Timer: Force page close
class PageFinisher(private val activity: Activity?, private val duration: Long) {
    private var countDownTimer: CountDownTimer? = null

    fun start() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onFinish() {
                activity?.finish()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
        countDownTimer?.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
    }
}
