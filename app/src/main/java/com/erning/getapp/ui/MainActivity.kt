package com.erning.getapp.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.erning.getapp.R
import com.erning.getapp.bean.AppBean
import com.erning.getapp.ui.base.BaseActivity
import com.erning.getapp.util.AppUtil
import com.erning.getapp.util.Config.*
import com.erning.getapp.util.RootCmd
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File

class MainActivity : BaseActivity() {
    private lateinit var data:List<AppBean>
    private var selectItem = -1
    private val adapter = Adapter(ArrayList())
    private var selectIsSystem = false
    private var mSearchView:SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        requestPermission()

        listview.setOnItemLongClickListener { _, _, position, _ ->
            selectItem = position
            false
        }
        listview.setOnItemClickListener { _, _, position, _ ->
            val select = adapter.getItem(position)

            val items = arrayOf("启动","打开某个页面","复制到储存卡","卸载","取消")
            AlertDialog.Builder(this)
                    .setTitle("你要做什么")
                    .setItems(items) { _, which ->
                        function(select,which)
                    }
                    .show()
        }
        listview.setOnCreateContextMenuListener { menu, v, menuInfo ->
            menuInflater.inflate(R.menu.menu, menu)
            super.onCreateContextMenu(menu, v, menuInfo)
        }

        showProgressDialog()
        doAsync {
            data = AppUtil.getAllApk(packageManager)
            runOnUiThread {
                hideProgressDialog()
                toast("共"+data.size+"个应用")
                val list = data.filter { it.isSystem.not() }
                adapter.setData(list)
                listview.adapter = adapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if(mSearchView == null){
            val searchItem = menu.findItem(R.id.menu_search)
            mSearchView = searchItem.actionView as SearchView
        }
        mSearchView?.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?:return false
                val list = data.filter { it.isSystem==selectIsSystem && (it.appName.contains(newText,true)||it.appPackageName.contains(newText,true)) }
                adapter.setData(list)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_user -> {
                selectIsSystem = false
                val list = data.filter { it.isSystem.not() }
                adapter.setData(list)
            }
            R.id.menu_system -> {
                selectIsSystem = true
                val list = data.filter { it.isSystem }
                adapter.setData(list)
            }
            R.id.menu_backup -> {
                startActivity<BackupActivity>()
            }
        }
        val mSearchAutoComplete = mSearchView?.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        mSearchAutoComplete?.setText("")
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (item == null || data.size <= selectItem){
            return super.onContextItemSelected(item)
        }
        val select = adapter.getItem(selectItem)
        function(select,item.itemId)
        return super.onContextItemSelected(item)
    }

    private fun function(app:AppBean,action:Int){
        when(action) {
            2, R.id.menu_1 -> {
                showProgressDialog()
                doAsync {
                    val file = File(app.apkPath)
                    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (downloadDir.exists().not()){
                        downloadDir.mkdirs()
                    }
                    val file2 = File(downloadDir,app.appName + ".apk")
                    file.copyTo(file2,true)
                    runOnUiThread {
                        toast("已复制到${file2.absolutePath}")
                        hideProgressDialog()
                    }
                }
            }
            0, R.id.menu_2 -> {
                AppUtil.openApp(this,app.appPackageName)
            }
            1, R.id.menu_3 -> {
                if (app.activities != null){
                    startActivity<ActivitiesActivity>(Pair("packName",app.appPackageName),Pair("appName",app.appName))
                }else{
                    toast("无可启动页面")
                }
            }
            3, R.id.menu_4 -> {
                if (app.isSystem && RootCmd.haveRootForTest()){
                    try {
                        unInstall(File(app.apkPath))
                    }catch (e:Exception){
                        e.printStackTrace()
                        val packageURI = Uri.parse("package:${app.appPackageName}")
                        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
                        startActivity(uninstallIntent)
                    }
                }else{
                    val packageURI = Uri.parse("package:${app.appPackageName}")
                    val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
                    startActivity(uninstallIntent)
                }
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, PERMISSION_WRITE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, PERMISSION_READ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(PERMISSION_WRITE,PERMISSION_READ), REQ_PERMISSION)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.isNotEmpty()){
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED){
                    finish()
                }
            }
        }else{
            finish()
        }
    }

    inner class Adapter(private val list: ArrayList<AppBean>):BaseAdapter(){
        override fun getItem(position: Int) = list[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getCount() = list.size

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item,parent,false)
            val icon = view.findViewById<ImageView>(R.id.icon)
            val name = view.findViewById<TextView>(R.id.name)
            val pack = view.findViewById<TextView>(R.id.pack)
            val size = view.findViewById<TextView>(R.id.size)
            val item = list[position]

            icon.setImageDrawable(item.appIcon)
            name.text = item.appName
            pack.text = item.appPackageName
            size.text = item.appSizeFormat

            return view
        }

        fun setData(data: List<AppBean>){
            list.clear()
            list.addAll(data)
            notifyDataSetChanged()
        }
    }

    private fun unInstall(file:File){
        Log.d("apk目录",file.absolutePath)
        val parent = file.parentFile
        var temp = File(filesDir.absolutePath + "/user")
        if (temp.exists().not()) temp.mkdir()
        temp = File(filesDir.absolutePath + "/system")
        if (temp.exists().not()) temp.mkdir()
        temp = File(filesDir.absolutePath + "/private")
        if (temp.exists().not()) temp.mkdir()

        val builder = AlertDialog.Builder(this)
        builder.setNegativeButton("取消",null)
        builder.setNeutralButton("我不知道",null)

        when {
            file.absolutePath.startsWith("/data/app/") -> {
                // 内置应用
                builder.setTitle("提醒")
                builder.setMessage("确定删除这一内置应用吗？")
                builder.setPositiveButton("确定") { _, _ ->
                    showProgressDialog()
                    doAsync {
                        RootCmd.execRootCmdSilent("rm -r ${filesDir.absolutePath}/private/${parent.name}")
                        RootCmd.execRootCmd("cp -r ${parent.absolutePath} ${filesDir.absolutePath}/user")
                        deleteDir(parent.absolutePath)
                    }
                }
            }
            file.absolutePath.startsWith("/system/app/") -> {
                // 系统应用
                builder.setTitle("注意")
                builder.setMessage("这不是内置应用而是系统应用，任然要删除吗？")
                builder.setPositiveButton("确定") { _, _ ->
                    showProgressDialog()
                    doAsync {
                        RootCmd.execRootCmdSilent("rm -r ${filesDir.absolutePath}/private/${parent.name}")
                        RootCmd.execRootCmd("cp -r ${parent.absolutePath} ${filesDir.absolutePath}/system")
                        deleteDir(parent.absolutePath)
                    }
                }
            }
            file.absolutePath.startsWith("/system/priv-app/") -> {
                // 系统核心应用
                builder.setTitle("警告")
                builder.setMessage("这可能是系统的一部分，任然要删除吗？")
                builder.setPositiveButton("确定") { _, _ ->
                    showProgressDialog()
                    doAsync {
                        RootCmd.execRootCmdSilent("rm -r ${filesDir.absolutePath}/private/${parent.name}")
                        RootCmd.execRootCmd("cp -r ${parent.absolutePath} ${filesDir.absolutePath}/private")
                        deleteDir(parent.absolutePath)
                    }
                }
            }
            else -> {
                builder.setTitle("奇怪")
                builder.setMessage("我也不知道这是啥,先观察一阵子，暂时还是不要轻举妄动了。\n${parent.absolutePath}")
                builder.setPositiveButton("确定", null)
            }
        }
        builder.show()
    }

    private fun deleteDir(path:String){
        if (path.startsWith("/system/app/")||path.startsWith("/system/priv-app/")){
            RootCmd.execRootCmd("mount -o remount,rw /system")//挂载为读写
            RootCmd.execRootCmd("rm -r $path")//删除
            RootCmd.execRootCmd("mount -o remount,ro /system")//挂载为只读
        }else {
            RootCmd.execRootCmd("rm -r $path")//删除
        }
        runOnUiThread {
            hideProgressDialog()
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("重启生效")
            builder.setMessage("你想现在就重启吗")
            builder.setNegativeButton("稍后",null)
            builder.setPositiveButton("重启") { _, _ ->
                RootCmd.execRootCmdSilent("reboot")
            }
            builder.show()
        }
    }
}