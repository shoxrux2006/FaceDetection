package uz.gita.facedetection.viewModel.impl

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import uz.gita.facedetection.viewModel.FaceRecognitionViewModel
import java.io.File

class FaceRecognitionViewModelImpl : ViewModel(), FaceRecognitionViewModel {
    override val cameraSelectorFlow: MutableStateFlow<CameraSelector> =
        MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    override val flashModeFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    override val backFlow: MutableSharedFlow<Unit> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun back() {
        backFlow.tryEmit(Unit)
    }

    var saveFile: File? = null
    override var file: File?
        get() = saveFile
        set(value) {
            saveFile = value
        }


    override fun cameraType(isFront: Boolean) {
        cameraSelectorFlow.value = if (isFront)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA
    }

    override fun flashMode(flashMode: Int) {
        flashModeFlow.value = flashMode

    }


}