package app.kalen.pyandroidcontrol

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private var rootBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootBtn = findViewById(R.id.root_btn)
        rootBtn!!.setOnClickListener{
            Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show()
        }
    }


}
