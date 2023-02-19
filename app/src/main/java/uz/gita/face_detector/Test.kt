package uz.gita.face_detector

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import uz.gita.facedetection.RecognitionScreen
import java.io.File

class Test : Fragment(R.layout.test) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btn = view.findViewById<Button>(R.id.btn_next)


        btn.setOnClickListener {
            val fragment = RecognitionScreen()
            lifecycleScope.launch {
                fragment.recognize(File("/storage/self/primary/DCIM/Camera/image.jpg")).collect {
                    Log.d("TTT", "ishladi")
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(this.id, fragment, RecognitionScreen::class.java.name)
                .addToBackStack("A")
                .commit()
        }
    }


}