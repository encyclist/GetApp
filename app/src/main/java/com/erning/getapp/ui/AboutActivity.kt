package com.erning.getapp.ui

import android.os.Bundle
import android.widget.Toast
import com.erning.getapp.BuildConfig
import com.erning.getapp.R
import com.erning.getapp.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*

class AboutActivity : BaseActivity() {
    private lateinit var toast: Toast
    private val array = arrayOf("😜","😍","😗","😈","🤣","😎","🤔","🙃","🙄","😫")
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        text_about_version.text = "Version：" + BuildConfig.VERSION_NAME

        toast = Toast.makeText(this,"😜😍😗😈🤣😎🤔🙃🙄😫",Toast.LENGTH_SHORT)
        text_about_erning.setOnClickListener {
            toast.cancel()
            toast = Toast.makeText(this,array[random.nextInt(array.size)],Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}