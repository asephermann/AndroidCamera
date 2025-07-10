package com.senjuid.camera

import android.annotation.SuppressLint
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
import com.senjuid.camera.databinding.ActivityCaptureBinding
import androidx.core.view.isVisible

/**
 * Created by Hendi, 19 Sep 2019
 * */
class CaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaptureBinding
    private lateinit var pageFinisher: PageFinisher
    private lateinit var helper: CaptureActivityHelper
    private lateinit var imageFileManager: ImageFileManager

    private val cameraListener = object : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            if (binding.cameraView.flash == Flash.TORCH) {
                binding.cameraView.flash = Flash.OFF
            }

            val maxSize = intent.getIntExtra("max_size", 0)
            helper.pictureResultHandler(result, maxSize) { bitmap ->
                // Check if the "disable_preview" extra in the intent is true
                if (intent.getBooleanExtra("disable_preview", false)) {
                    try {
                        val disableMirror = intent.getBooleanExtra("disable_mirror", true)
                        if (binding.cameraView.facing == Facing.FRONT && disableMirror) {
                            helper.flip(-1f, 1f) {
                                helper.saveBitmapAndFinish(intent) {
                                    val returnIntent = Intent()
                                    returnIntent.putExtra("photo", it)
                                    setResult(RESULT_OK, returnIntent)
                                    finish()
                                }
                            }
                        } else {
                            helper.saveBitmapAndFinish(intent) {
                                val returnIntent = Intent()
                                returnIntent.putExtra("photo", it)
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }
                        }
                    } catch (_: Exception) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("photo", "")
                        returnIntent.putExtra("native", true)
                        returnIntent.putExtra("crash", true)
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    }
                } else {
                    val disableMirror = intent.getBooleanExtra("disable_mirror", true)
                    if (binding.cameraView.facing == Facing.FRONT && disableMirror) {
                        helper.flip(-1f, 1f) {
                            binding.ivPreview.setImageBitmap(it)
                        }
                    } else {
                        binding.ivPreview.setImageBitmap(bitmap)
                    }
                    showProgressDialog(false)
                    viewMode(false)
                }
            }
        }
    }

    // MARK: Lifecycle
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.cameraView.setLifecycleOwner(this)
            binding.cameraView.addCameraListener(cameraListener)
            binding.cameraView.setPictureSize(SizeSelectors.smallest())

            // Add take picture button listener
            binding.btnTakePicture.setOnClickListener {
                var delay: Long = 0
                if (binding.btnFlashOn.isVisible && binding.cameraView.facing == Facing.BACK) {
                    binding.cameraView.flash = Flash.TORCH
                    delay = 1000
                }
                showProgressDialog(true)
                Handler().postDelayed({
                    binding.cameraView.playSounds = isAudioServiceMute()
                    val snapshot = intent.extras!!.getBoolean("is_snapshot", true)
                    if (snapshot) {
                        binding.cameraView.takePictureSnapshot() // faster
                    } else {
                        binding.cameraView.takePicture()
                    }
                }, delay)
            }

            // Add back button listener
            binding.btnBack.setOnClickListener {
                finish()
            }

            // Add retake button listener
            binding.btnRetake.setOnClickListener {
                if (binding.btnFlashOn.isVisible) {
                    binding.cameraView.flash = Flash.TORCH
                }
                viewMode(true)
            }

            // Add rotate button listener
            binding.btnRotatePicture.setOnClickListener {
                helper.rotate(binding.ivPreview.rotation + 90) {
                    binding.ivPreview.setImageBitmap(it)
                }
            }

            // Add select picture button listener
            binding.btnSelectPicture.setOnClickListener {
                try {
                    helper.saveBitmapAndFinish(intent) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("photo", it)
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    }
                } catch (_: Exception) {
                    val returnIntent = Intent()
                    returnIntent.putExtra("photo", "")
                    returnIntent.putExtra("native", true)
                    returnIntent.putExtra("crash", true)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }
            }

            binding.btnFlashOn.setOnClickListener {
                binding.cameraView.flash = Flash.OFF
                binding.btnFlashOn.visibility = View.GONE
                binding.btnFlashOff.visibility = View.VISIBLE
            }

            binding.btnFlashOff.setOnClickListener {
                binding.cameraView.flash = Flash.TORCH
                binding.btnFlashOn.visibility = View.VISIBLE
                binding.btnFlashOff.visibility = View.GONE
            }

            // set view mode
            viewMode(true)

            // Check if the "disable_back" extra in the intent is true
            if (intent.getBooleanExtra("disable_back", false)) {
                // Disable back camera functionality
                binding.cameraView.facing = Facing.FRONT
                binding.tvCamera.text = "Cam 1"
                // Set visibility of face silhouette based on the "show_face_area" extra in the intent
                binding.faceSilhouette.visibility =
                    if (intent.getBooleanExtra("show_face_area", false)) View.VISIBLE else View.GONE
                binding.btnSwitchCamera.visibility = View.GONE
            } else {
                // Set camera facing based on the "facing_back" extra in the intent
                if (intent.getBooleanExtra("facing_back", true)) {
                    binding.cameraView.facing = Facing.BACK
                    binding.tvCamera.text = "Cam 2"
                } else {
                    binding.cameraView.facing = Facing.FRONT
                    binding.tvCamera.text = "Cam 1"
                }

                // Set visibility of face silhouette based on the camera facing and "show_face_area" extra
                if (intent.getBooleanExtra(
                        "show_face_area",
                        false
                    ) && binding.cameraView.facing == Facing.FRONT
                ) {
                    binding.faceSilhouette.visibility = View.VISIBLE
                } else {
                    binding.faceSilhouette.visibility = View.GONE
                }

                // Set visibility and click listener for the switch camera button
                binding.btnSwitchCamera.visibility = View.VISIBLE
                binding.btnSwitchCamera.setOnClickListener {
                    binding.cameraView.toggleFacing()
                    if (binding.cameraView.facing == Facing.FRONT) {
                        binding.tvCamera.text = "Cam 1"
                    } else {
                        binding.tvCamera.text = "Cam 2"
                    }
                    if (intent.getBooleanExtra(
                            "show_face_area",
                            false
                        ) && binding.cameraView.facing == Facing.FRONT
                    ) {
                        binding.faceSilhouette.visibility = View.VISIBLE
                    } else {
                        binding.faceSilhouette.visibility = View.GONE
                    }
                }
            }

            binding.tvCamera.setOnClickListener {
                val returnIntent = Intent()
                returnIntent.putExtra("photo", "")
                returnIntent.putExtra("native", true)
                returnIntent.putExtra("crash", true)
                setResult(RESULT_OK, returnIntent)
                finish()
            }

        } catch (_: Exception) {
            val data = Intent().apply {
                putExtra("photo", "")
                putExtra("native", false)
                putExtra("crash", true)
            }
            setResult(RESULT_OK, data)
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
        binding.layoutProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun viewMode(isCapture: Boolean) {
        if (isCapture) {
            binding.layoutPreview.visibility = View.GONE
        } else {
            binding.layoutPreview.visibility = View.VISIBLE
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
