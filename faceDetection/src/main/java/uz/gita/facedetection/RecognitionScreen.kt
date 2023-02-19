package uz.gita.facedetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.gita.facedetection.data.CaptureData
import uz.gita.facedetection.data.FaceData
import uz.gita.facedetection.drawer.DrawFaceLine
import uz.gita.facedetection.util.Const
import uz.gita.facedetection.viewModel.FaceRecognitionViewModel
import uz.gita.facedetection.viewModel.impl.FaceRecognitionViewModelImpl
import java.io.File
import java.util.concurrent.Executors

class RecognitionScreen : Fragment(R.layout.recognition_screen) {
    private var preview: Preview? = null
    private var isFront: Boolean = false
    private var isCapture: Boolean = true
    private var flashMode: Int = 0
    private var btnSwitch: ImageButton? = null
    private var btnBack: ImageButton? = null
    private var btnFlash: ImageButton? = null
    private lateinit var text: TextView
    private  var custom: DrawFaceLine?=null
    private var shp: SharedPreferences? = null
    private lateinit var cameraSelector: CameraSelector
    private var savedFile: File? = null
    private val savedFileFlow: MutableSharedFlow<File> =
        MutableSharedFlow(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val viewModel: FaceRecognitionViewModel by viewModels<FaceRecognitionViewModelImpl>()
    private lateinit var cameraProcessFuture: ListenableFuture<ProcessCameraProvider>
    private var imageCapture: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shp = requireContext().getSharedPreferences("recognizeShp", Context.MODE_PRIVATE)
        imageCapture = ImageCapture.Builder().build()
        Log.d("TTT", "viewModelga berildi${savedFile}")
        savedFile?.let { viewModel.file = it }
        viewModel.backFlow.onEach {

            requireActivity().supportFragmentManager.popBackStack()
        }.launchIn(lifecycleScope)
    }



    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        preview = Preview.Builder().build()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        shp?.let {
            viewModel.flashMode(it.getInt(Const.flashMode, 0))
            viewModel.cameraType(it.getBoolean(Const.isFront, false))
        }

        preview?.setSurfaceProvider(view1.findViewById<PreviewView>(R.id.preView).surfaceProvider)
        btnSwitch = view1.findViewById(R.id.btn_switch)
        btnBack = view1.findViewById(R.id.btn_back)
        btnFlash = view1.findViewById(R.id.btn_flash)
        text = view1.findViewById(R.id.text)
        custom = view1.findViewById(R.id.myView)
        btnBack?.setOnClickListener {
            viewModel.back()
        }

        btnFlash?.setOnClickListener {
            when (flashMode) {
                0 -> {
                    viewModel.flashMode(1)
                }
                1 -> {
                    viewModel.flashMode(2)
                }
                2 -> {
                    viewModel.flashMode(0)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.cameraSelectorFlow.collect {
                custom?.isFront = when (it) {
                    CameraSelector.DEFAULT_BACK_CAMERA -> {
                        shp?.apply { edit().putBoolean(Const.isFront, false).apply() }
                        false
                    }
                    CameraSelector.DEFAULT_FRONT_CAMERA -> {
                        shp?.apply { edit().putBoolean(Const.isFront, true).apply() }
                        true
                    }
                    else -> {
                        false
                    }
                }

                isFront = custom?.isFront == true
                cameraSelector = it
                initPreview()
            }
        }
        lifecycleScope.launch {
            viewModel.flashModeFlow.collectLatest {
                when (it) {
                    0 -> {
                        flashMode = 0
                        imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                        btnFlash?.setImageResource(R.drawable.baseline_flash_off_24)
                    }
                    1 -> {
                        flashMode = 1
                        imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                        btnFlash?.setImageResource(R.drawable.baseline_flash_auto_24)
                    }
                    2 -> {
                        flashMode = 2
                        imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                        btnFlash?.setImageResource(R.drawable.baseline_flash_on_24)
                    }
                }
                shp?.apply { edit().putInt(Const.flashMode, it).apply() }
            }
        }




        btnSwitch?.setOnClickListener {
            viewModel.cameraType(!isFront)

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent =
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        Dexter.withContext(requireContext())
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    initPreview()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {

                }

            }).check()
    }

    override fun onDestroy() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        btnSwitch = null
        btnFlash = null
        btnBack = null
        preview=null
        super.onDestroy()
    }

    fun recognize(file: File): Flow<File> {
        Log.d("TTT", "file olindi")
        savedFile = file
        return savedFileFlow
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initPreview() {
        val mainExecutor = Executors.newSingleThreadExecutor()
        cameraProcessFuture = ProcessCameraProvider.getInstance(requireContext())
        val isPortrait = requireActivity().resources.configuration.orientation == 1
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(if (isPortrait) 720 else 1280, if (isPortrait) 1280 else 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val options = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(100f)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        val detector: FaceDetector = FaceDetection.getClient(options)
        imageAnalysis.setAnalyzer(mainExecutor) { imageProxy ->
            val degrees = imageProxy.imageInfo.rotationDegrees
            var image = imageProxy.image
            if (image != null) {
                val imageValue = InputImage.fromMediaImage(image, degrees)
                custom?.imageHeight =
                    if (isPortrait) imageValue.width else imageValue.height
                custom?.imageWidth = if (isPortrait) imageValue.height else imageValue.width
                detector.process(imageValue)
                    .addOnSuccessListener { faces ->
                        lifecycleScope.launch {
                            when (faces.size) {

                                1 -> {
                                    faceContourDetectionResult(
                                        faces[0],
                                        isPortrait,
                                        custom!!
                                    ).collect {
                                        when (it) {
                                            is CaptureData.Error -> {
                                                custom?.roundRectPaint!!.color = Color.RED
                                                custom?.rectPaint!!.color = Color.RED
                                                custom?.invalidate()
                                                text.text = it.message
                                            }
                                            is CaptureData.LoadFaceData -> {
                                                custom?.draw(it.faceData)
                                            }
                                            CaptureData.Success -> {
                                                custom?.roundRectPaint!!.color = Color.GREEN
                                                custom?.rectPaint!!.color = Color.GREEN
                                                custom?.invalidate()
                                                text.text = "Qimirlamanag rasmga olish boshlandi"
                                                delay(2000)
                                                if (isCapture)
                                                    viewModel.file?.let { its ->
                                                        Log.d("TTT", "nullmas")
                                                        capture(its)
                                                    }
                                                isCapture = false
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                0 -> {
                                    custom?.roundRectPaint!!.color = Color.RED
                                    custom?.rectPaint!!.color = Color.RED
                                    val faceData = FaceData(
                                        mutableListOf(), Rect(),
                                        Color.GREEN
                                    )
                                    custom?.draw(faceData)
                                    text.text = "Yuz mavjud emas"
                                }
                                else -> {

                                    custom?.roundRectPaint!!.color = Color.RED
                                    custom?.rectPaint!!.color = Color.RED
                                    val faceData = FaceData(
                                        mutableListOf(), Rect(),
                                        Color.GREEN
                                    )
                                    custom?.draw(faceData)
                                    text.text = "Yuzlar soni 1tadan ko'p"
                                }
                            }
                        }
                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
                        imageProxy.close()
                        e.printStackTrace()
                    }
            }

        }

        cameraProcessFuture.addListener(
            {
                val cameraProvider = cameraProcessFuture.get()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this as LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis, imageCapture
                    )
                } catch (e: Exception) {

                }

            }, ContextCompat.getMainExecutor(requireContext())
        )


    }


    private fun capture(file: File) {
        lifecycleScope.launch(Dispatchers.IO) {
            cameraProcessFuture = ProcessCameraProvider.getInstance(requireContext())

            val imageCapture = imageCapture ?: return@launch
            val outPutOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(
                outPutOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        savedFileFlow.tryEmit(file)
                        requireActivity().supportFragmentManager.popBackStack()
                    }

                    override fun onError(exception: ImageCaptureException) {
                    }

                })
        }
    }
}



