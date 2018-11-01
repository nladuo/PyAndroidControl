package app.kalen.pyandroidcontrol


import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.jaredrummler.android.shell.Shell
import java.util.*


class BackgroundService : Service() {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var button: Button? = null

    private var performClick = true
    private var isMoving = false
    private var taskStarted = false

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams!!.format = PixelFormat.RGBA_8888
        layoutParams!!.gravity = Gravity.START or Gravity.TOP
        layoutParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams!!.width = 250
        layoutParams!!.height = 140
        layoutParams!!.x = 300
        layoutParams!!.y = 300
    }

    override fun onBind(intent: Intent): IBinder? {
        BackgroundService.isStarted = true
        showFloatingWindow()

        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        windowManager!!.removeView(button)
        BackgroundService.isStarted = false
        return super.onUnbind(intent)
    }

    private fun showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                return
            }
        }
        button = Button(applicationContext)
        button!!.setText(R.string.startTxt)
        windowManager!!.addView(button, layoutParams)

        button!!.setOnTouchListener(FloatingOnTouchListener())

        button!!.setOnClickListener {
            println("ACTION_clicked")
            println(performClick)
            if (performClick) {
                if (!taskStarted) {
                    button!!.setText(R.string.stopTxt)
                    Toast.makeText(applicationContext, "Task Started!!", Toast.LENGTH_SHORT).show()
                    // start a thread to run in backend.
                    Thread(Runnable {
                        val rnd = Random()
                        while (this@BackgroundService.taskStarted) {
                            try {
                                Thread.sleep(NetUtils.interval)
                                val cmds = NetUtils.uploadScreenshot()

                                for (cmd in cmds) {
                                    Shell.SU.run(cmd)
                                    // random sleep 50-150ms
                                    val rndTime:Long = (50 + rnd.nextInt(100)).toLong()
                                    Thread.sleep(rndTime)
                                }
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }).start()
                } else {
                    button!!.setText(R.string.startTxt)
                    Toast.makeText(applicationContext, "Task Stopped!!", Toast.LENGTH_SHORT).show()
                }
                taskStarted = !taskStarted
            }
        }
    }


    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x: Int = 0
        private var y: Int = 0


        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    println("ACTION_DOWN")
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    println("ACTION_MOVE")
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()

                    // when clicked also trigger the ACTION_MOVE
                    if ((nowX != x) or (nowY != y)) {
                        isMoving = true
                    }
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    layoutParams!!.x = layoutParams!!.x + movedX
                    layoutParams!!.y = layoutParams!!.y + movedY
                    windowManager!!.updateViewLayout(view, layoutParams)
                }
                MotionEvent.ACTION_UP -> {
                    performClick = !isMoving
                    isMoving = false
                    println("ACTION_UP")
                }
                else -> {
                }
            }
            return false
        }
    }

    companion object {
        var isStarted = false
    }
}
