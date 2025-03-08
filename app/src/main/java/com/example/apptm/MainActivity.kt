package com.example.apptm

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptm.databinding.ActivityMainBinding
import com.ingenieriiajhr.jhrCameraX.BitmapResponse
import com.ingenieriiajhr.jhrCameraX.CameraJhr


class MainActivity : AppCompatActivity() {



    lateinit var binding : ActivityMainBinding
//    lateinit var binding : ActivityMainBinding
    lateinit var cameraJhr: CameraJhr
    lateinit var classifyImageTf: ClassifyImageTf
//    lateinit var classifyImageTf: ClassifyImageTf

    companion object {
        const val INPUT_SIZE = 224
        const val OUTPUT_SIZE = 3 // Número de etiquetas de salida
    }

    val classes = arrayOf("DICE", "RUBIK", "NORMAL")

    //224 x 224
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init cameraJHR
        cameraJhr = CameraJhr(this)

        classifyImageTf = ClassifyImageTf(this)

    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (cameraJhr.allpermissionsGranted() && !cameraJhr.ifStartCamera){
            startCameraJhr()
        }else{
            cameraJhr.noPermissions()
        }
    }

    /**
     * start Camera Jhr
     */
    private fun startCameraJhr() {
        var timeRepeat = System.currentTimeMillis()
        cameraJhr.addlistenerBitmap(object : BitmapResponse {
            override fun bitmapReturn(bitmap: Bitmap?) {
                if (bitmap!=null){
                    if (System.currentTimeMillis()>timeRepeat+1000){
                        classifyImage(bitmap)
                        timeRepeat = System.currentTimeMillis()
                    }

                }
            }
        })
        cameraJhr.initBitmap()
        cameraJhr.initImageProxy()
        //selector camera LENS_FACING_FRONT = 0;    LENS_FACING_BACK = 1;
        //aspect Ratio  RATIO_4_3 = 0; RATIO_16_9 = 1;  false returImageProxy, true return bitmap
        cameraJhr.start(0,0,binding.cameraPreview,true,false,true)
    }

    private fun classifyImage(img: Bitmap?) {
        val imgReScale = Bitmap.createScaledBitmap(img!!, INPUT_SIZE, INPUT_SIZE,false)

        classifyImageTf.listenerInterpreter(object :ReturnInterpreter{
            override fun classify(confidence: FloatArray, maxConfidence: Int) {
                binding.txtResult.UiThread("Dice ${confidence[0].decimal()} \n Rubik ${confidence[1].decimal()} \n Normal ${confidence[2].decimal()} \n MaxPos ${classes[maxConfidence]}")
            }
        })
//        classifyImageTf.listenerInterpreter(object :ReturnInterpreter{
//            override fun classify(confidence: FloatArray, maxConfidence: Int) {
//                runOnUiThread {
//                    binding.txtResult.text = "${classes[0]}: ${confidence[0]}"
//                }
//            }
//
//        })
        /**Preview Image Get */
        runOnUiThread {
            binding.imgPreview.setImageBitmap(imgReScale)
        }
        classifyImageTf.classify(imgReScale)


    }

    private fun TextView.UiThread(string: String){
        runOnUiThread {
            this.text = string
        }
    }

    private fun Float.decimal():String{
        return "%.2f".format(this)
    }


}