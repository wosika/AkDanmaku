package com.kuaishou.akdanmaku.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.ecs.DanmakuEngine
import com.kuaishou.akdanmaku.render.SimpleRenderer
import com.kuaishou.akdanmaku.render.TypedDanmakuRenderer
import com.kuaishou.akdanmaku.ui.DanmakuPlayer
import com.kuaishou.akdanmaku.ui.DanmakuView
import java.lang.ref.WeakReference

class SampleFullScreenActivity : AppCompatActivity() {

    companion object {
        private const val MSG_START = 1001
        private const val MSG_UPDATE_DATA = 2001
    }

    private var danmakuPlayer: WeakReference<DanmakuPlayer>? = null
    private var danmakuPlayController: WeakReference<DanmakuPlayController>? = null

    private val byStep by lazy { intent?.getBooleanExtra("byStep", false) ?: false }

    private var paused = false
    private val simpleRenderer = SimpleRenderer()
    private val renderer by lazy {
        TypedDanmakuRenderer(
            simpleRenderer,
            DanmakuItemData.DANMAKU_STYLE_ICON_UP to UpLogoRenderer(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.icon_danmaku_input_text_up_icon,
                    theme
                )!!
            )
        )
    }

    private val danmakuView by lazy { findViewById<DanmakuView>(R.id.danmakuView) }

//    private val mainHandler = object : Handler(Looper.getMainLooper()) {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                MSG_START -> danmakuPlayController?.get()?.start()
//                MSG_UPDATE_DATA -> updateDanmakuData()
//            }
//        }
//    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sample_full_creen)

        danmakuPlayer = WeakReference(DanmakuPlayer(renderer).also {
            it.bindView(danmakuView)
        })
        danmakuPlayController = WeakReference(DanmakuPlayController(this, danmakuPlayer?.get()!!, byStep))
        updateDanmakuData()
        danmakuPlayController?.get()?.start()

      //  mainHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA, 0)
       // mainHandler.sendEmptyMessageDelayed(MSG_START, 100)
    }

    override fun onResume() {
        super.onResume()

        if (paused) {
            danmakuPlayController?.get()?.start()
            paused = false
        }
    }

    override fun onPause() {
        super.onPause()


        danmakuPlayController?.get()?.pause()
        paused = true
    }

    override fun onDestroy() {
        super.onDestroy()

    //    mainHandler.removeCallbacksAndMessages(null)
        danmakuPlayController?.clear()
        danmakuPlayController = null

        danmakuPlayer?.get()?.release()
        danmakuPlayer= null
    }

    private fun updateDanmakuData() {
        Log.d(DanmakuEngine.TAG, "[Sample] 开始加载数据")
        val jsonString =
            assets.open("test_danmaku_data.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<DanmakuItemData>>() {}.type
        Log.d(DanmakuEngine.TAG, "[Sample] 开始解析数据")
        val dataList = Gson().fromJson<List<DanmakuItemData>>(jsonString, type)
        danmakuPlayer?.get()?.updateData(dataList)
        Log.d(DanmakuEngine.TAG, "[Sample] 数据已加载(count = ${dataList.size})")
//    danmakuView.post {
//      Toast.makeText(this, "开始加载数据", Toast.LENGTH_SHORT).show()
//    }
    }
}
