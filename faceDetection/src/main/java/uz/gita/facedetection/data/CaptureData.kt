package uz.gita.facedetection.data

sealed class CaptureData {
    data class LoadFaceData(val faceData: FaceData) : CaptureData()
    data class Error(val message: String) : CaptureData()
    object Success : CaptureData()
}