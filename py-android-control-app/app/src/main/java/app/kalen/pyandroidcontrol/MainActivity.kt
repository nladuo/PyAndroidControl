package app.kalen.pyandroidcontrol

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.jaredrummler.android.shell.Shell
import com.jaredrummler.android.shell.CommandResult
import android.provider.Settings
import android.os.Build
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.IBinder
import android.view.View
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var checkBtn: Button? = null
    private var startBtn: Button? = null
    private var stopBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // check the control server connection
        checkBtn = findViewById(R.id.check_btn)
        checkBtn!!.setOnClickListener(this)

        // start the service
        startBtn = findViewById(R.id.start_btn)
        startBtn!!.setOnClickListener(this)

        // shutdown the service
        stopBtn = findViewById(R.id.terminate_btn)
        stopBtn!!.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.check_btn -> {
                val task = GetTokenTask()
                task.execute()
            }

            R.id.start_btn -> {
                startBGService()
            }

            R.id.terminate_btn ->{
                if (BackgroundService.isStarted) {
                    unbindService(conn)
                }
            }
        }
    }

    private var conn: ServiceConnection? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val noReadPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                if (!Settings.canDrawOverlays(this) or noReadPermission) {
                    Toast.makeText(this, "Authorization failure", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Authorization success", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val noReadPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

            if (!Settings.canDrawOverlays(this) or noReadPermission) {
                val dialog = AlertDialog.Builder(this).create()
                dialog.setTitle("Permission Note")
                dialog.setMessage("Please allow File Access Permission and Floating Window Permission")
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK") { _, _ ->
                    startActivityForResult(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        ), 0
                    )
                }
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel") { _, _ ->
                    finish()
                }
                dialog.show()
                return false
            }
        }
        return true
    }


    private fun startBGService() {
        // check read, write and float window permission
        if (!checkPermission()){
            return
        }

        // check root permission
        val result: CommandResult = Shell.SU.run("id")
        println(result.isSuccessful)
        if (!result.isSuccessful) {
            Toast.makeText(this, "No Root Permission", Toast.LENGTH_LONG).show()
            return
        }

        if (BackgroundService.isStarted) {
            return
        }

        conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
            }

            override fun onServiceDisconnected(name: ComponentName) {
            }
        }

        bindService(
            Intent(this@MainActivity, BackgroundService::class.java),
            conn, Context.BIND_AUTO_CREATE
        )

    }


    private inner class GetTokenTask: AsyncTask<Void, Void, Int>() {

        override fun onPreExecute() {
            super.onPreExecute()


        }

        override fun doInBackground(vararg params: Void?): Int {
            NetUtils.getToken()

            return 1
        }
    }


}
