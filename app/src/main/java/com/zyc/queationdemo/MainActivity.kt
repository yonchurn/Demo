package com.zyc.queationdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.common.base.extension.setOnSingleListener
import com.zyc.queationdemo.nested.NestedScrollActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.btn).setOnSingleListener {
            startActivity(Intent(this, NestedScrollActivity::class.java))
        }
    }
}