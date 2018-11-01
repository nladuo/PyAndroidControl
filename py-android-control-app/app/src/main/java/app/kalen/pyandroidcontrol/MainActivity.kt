package app.kalen.pyandroidcontrol

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.jaredrummler.android.shell.Shell
import com.jaredrummler.android.shell.CommandResult
import android.provider.Settings
import android.os.Build
import android.content.ComponentName
import android.net.Uri
import android.os.IBinder







class MainActivity : AppCompatActivity() {

    private var startBtn: Button? = null
    private var stopBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBtn = findViewById(R.id.start_btn)
        startBtn!!.setOnClickListener{
            startBGService()

//            var result: CommandResult = Shell.SU.run("id")
//            if (result.isSuccessful) {
//                println(result.getStdout())
//            }
//
//            result = Shell.SU.run("/system/bin/screencap -p /sdcard/scre666enshot.png > /dev/null")
//            if (result.isSuccessful) {
//                println(result.getStdout())
//            }
        }

        stopBtn = findViewById(R.id.terminate_btn)
        stopBtn!!.setOnClickListener {
            if (BackgroundService.isStarted) {
                unbindService(conn)
            }
        }

    }

    private var conn: ServiceConnection? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Authorization failure", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Authorization success", Toast.LENGTH_SHORT).show()
                    bindService(
                        Intent(this@MainActivity, BackgroundService::class.java),
                        conn, Context.BIND_AUTO_CREATE
                    )
                }
            }
        }
    }

    fun startBGService() {

        if (BackgroundService.isStarted) {
            return
        }

        conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {

            }

            override fun onServiceDisconnected(name: ComponentName) {

            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show()
                startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ), 0
                )
            } else {
                bindService(
                    Intent(this@MainActivity, BackgroundService::class.java),
                    conn, Context.BIND_AUTO_CREATE
                )
            }
        } else {
            bindService(
                Intent(this@MainActivity, BackgroundService::class.java),
                conn, Context.BIND_AUTO_CREATE
            )
        }

    }


}
