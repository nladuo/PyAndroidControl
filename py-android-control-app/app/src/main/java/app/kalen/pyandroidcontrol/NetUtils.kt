package app.kalen.pyandroidcontrol

import android.os.Environment
import com.jaredrummler.android.shell.Shell
import okhttp3.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

object NetUtils {

    var BASE_URL = ""
    var token = ""
    var interval:Long = 1000

    /**
     * upload the screenshot to the control server
     */
    fun uploadScreenshot(): List<String>{
        val commands = ArrayList<String>()
        val sdcardPath = Environment.getExternalStorageDirectory().path
        val imagePath = "$sdcardPath/py-android-control.png"
        println(imagePath)

        val result = Shell.SU.run("/system/bin/screencap -p $imagePath > /dev/null")
        if (result.isSuccessful) {
            println(result.getStdout())
        }

        val client = OkHttpClient()
        val url =  "$BASE_URL/upload_screenshot?token=$token"
        val file = File(imagePath)

        val fileBody = RequestBody.create(MediaType.parse("image/png"), file)

        val reqBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "py-android-control.png", fileBody)
            .build()


        val req = Request.Builder()
            .url(url)
            .post(reqBody)
            .build()

        val call = client.newCall(req)
        try {
            val resp = call.execute()
            val body = resp.body()!!.string()
            val tokener = JSONTokener(body)

            val jsonObj = JSONObject(tokener)
            val cmdArray = jsonObj.getJSONArray("commands")

            var i = 0
            while (i < cmdArray.length()) {
                val cmd = cmdArray.get(i).toString()
                commands.add(cmd)
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        println(commands)

        return commands
    }

    /**
     * get login token_id from control server
     */
    fun getToken() {
        val client = OkHttpClient()
        val url =  "$BASE_URL/login"
        println(url)
        val req = Request.Builder().url(url).build()
        val call = client.newCall(req)

        try {
            val resp = call.execute()
            println(resp.body()!!.string())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}