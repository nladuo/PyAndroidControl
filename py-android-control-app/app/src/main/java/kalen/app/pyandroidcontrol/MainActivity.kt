package kalen.app.pyandroidcontrol

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
import android.widget.EditText
import android.app.ProgressDialog
import android.widget.TextView
import java.lang.Exception


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var checkBtn: Button? = null
    private var startBtn: Button? = null
    private var stopBtn: Button? = null
    private var urlEdit: EditText? = null
    private var showTView: TextView? = null
    private var sp: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initInput()

        showTView = findViewById(R.id.show_tview)

        // check the control server connection
        checkBtn = findViewById(R.id.check_btn)
        checkBtn!!.setOnClickListener(this)

        // start the service
        startBtn = findViewById(R.id.start_btn)
        startBtn!!.setOnClickListener(this)

        // shutdown the service
        stopBtn = findViewById(R.id.terminate_btn)
        stopBtn!!.setOnClickListener(this)

        checkPermission()
    }

    /**
     * init BaseUrl EditText
     */
    private fun initInput() {
        urlEdit = findViewById(R.id.url_input_edit)

        sp = getSharedPreferences("Default", Context.MODE_PRIVATE)
        NetUtils.BASE_URL = sp!!.getString("base_url", "")
        urlEdit!!.setText(NetUtils.BASE_URL)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.check_btn -> {
                var urlEditStr = urlEdit!!.text.toString()

                if (urlEditStr.endsWith("/")) {
                    urlEditStr = urlEditStr.substring(0, urlEditStr.length-1)
                    urlEdit!!.setText(urlEditStr)
                }

                val editor = sp!!.edit()
                NetUtils.BASE_URL = urlEditStr
                editor.putString("base_url", NetUtils.BASE_URL)
                editor.apply()
                val task = GetTokenTask(this)
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

    /**
     * check the READ_EXTERNAL_STORAGE and SYSTEM_OVERLAY_WINDOW Permission
     */
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


    /**
     * Start Background Service
     */
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


    private inner class GetTokenTask(internal var context: Context): AsyncTask<Void, Void, Int>() {

        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog.show(context, "Loading",
                "checking the connectivity from Control Server", true, false)
        }

        override fun doInBackground(vararg params: Void?): Int {
            try {
                NetUtils.getToken()
            }catch (e: Exception) {
                e.printStackTrace()
                NetUtils.token = ""
            }

            Thread.sleep(1000) // sleep 1 second to make UI more friendly
            return 1
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            progressDialog!!.dismiss()
            if (NetUtils.token != "") {
                showTView!!.text = "token:${NetUtils.token}\ninterval:${NetUtils.interval}"
                startBtn!!.isEnabled = true
                stopBtn!!.isEnabled = true
            } else{
                showTView!!.text = ""
                startBtn!!.isEnabled = false
                stopBtn!!.isEnabled = false
                val dialog = AlertDialog.Builder(context).create()
                dialog.setTitle("Connection Failure")
                dialog.setMessage("Failed to login the control Server")
                dialog.show()
            }
        }
    }
}
