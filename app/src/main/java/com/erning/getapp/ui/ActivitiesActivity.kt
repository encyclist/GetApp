package com.erning.getapp.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.erning.getapp.R
import com.erning.getapp.ui.base.BaseActivity
import com.erning.getapp.util.AppUtil
import kotlinx.android.synthetic.main.activity_activities.*
import org.jetbrains.anko.doAsync

class ActivitiesActivity : BaseActivity() {
    private lateinit var activities:List<ActivityInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activities)
        supportActionBar?.title = intent.getStringExtra("appName")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        list_activity.setOnItemClickListener { _, _, i, _ ->
            val item = activities[i]
            try {
                val intent = Intent()
                intent.component = ComponentName(item.packageName,item.name)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@ActivitiesActivity, "哔了哈士奇了", Toast.LENGTH_SHORT).show()
            }
        }

        showProgressDialog()
        doAsync {
            activities = AppUtil.getAllActivity(this@ActivitiesActivity,intent.getStringExtra("packName"))
            runOnUiThread {
                hideProgressDialog()
                list_activity.adapter = Adapter()
            }
        }
    }

    inner class Adapter:BaseAdapter(){
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val view = p1 ?: layoutInflater.inflate(R.layout.item_ac,p2,false)
            val tv = view.findViewById<TextView>(R.id.textView)
            val item = activities[p0]

            val label = item.nonLocalizedLabel
            if (label != null){
                tv.text = "${item.name}(${label})"
            }else{
                tv.text = item.name
            }
            tv.setTextColor(if (item.exported) Color.BLACK else Color.GRAY)

            return view
        }
        override fun getItem(p0: Int) = activities[p0]
        override fun getItemId(p0: Int) = p0.toLong()
        override fun getCount() = activities.size
    }
}
