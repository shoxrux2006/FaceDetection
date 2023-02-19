package uz.gita.facedetection.viewModel

import androidx.camera.core.CameraSelector
import kotlinx.coroutines.flow.Flow
import java.io.File


interface FaceRecognitionViewModel {
    val cameraSelectorFlow: Flow<CameraSelector>
    val flashModeFlow: Flow<Int>
    val backFlow:Flow<Unit>
    fun back()
   var file: File?
    fun cameraType(isFront: Boolean)
    fun flashMode(flashMode: Int)
}