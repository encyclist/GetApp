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

        requestPermission()

        listview.setOnItemLongClickListener { _, _, position, _ ->
            selectItem = position
            false
        }
        listview.setOnItemClickListener { _, _, position, _ ->
            val select = adapter.getItem(position)

            AlertDialog.Builder(this)
                    .setTitle(R.string.what_are_you_doing)
                    .setItems(R.array.action_main) { _, which ->
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
                toast(String.format(getString(R.string.total_apps),data.size))
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
            R.id.menu_about ->{
                startActivity<AboutActivity>()
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
                        toast(String.format(getString(R.string.copy_result),file2.absolutePath))
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
                    toast(R.string.no_launchable_page)
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
            val target = view.findViewById<TextView>(R.id.target)
            val item = list[position]

            icon.setImageDrawable(item.appIcon)
            name.text = item.appName
            pack.text = item.appPackageName
            size.text = item.appSizeFormat
            target.text = String.format(getString(R.string.target),item.targetSdkVersion)

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
        builder.setNegativeButton(R.string.cancel,null)
        builder.setNeutralButton(R.string.not_know,null)

        when {
            file.absolutePath.startsWith("/data/app/") -> {
                // 内置应用
                builder.setTitle(R.string.remind)
                builder.setMessage(R.string.delete_built_in_application)
                builder.setPositiveButton(R.string.determine) { _, _ ->
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
                builder.setTitle(R.string.note)
                builder.setMessage(R.string.delete_system_app)
                builder.setPositiveButton(R.string.determine) { _, _ ->
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
                builder.setTitle(R.string.caveat)
                builder.setMessage(R.string.delete_system)
                builder.setPositiveButton(R.string.determine) { _, _ ->
                    showProgressDialog()
                    doAsync {
                        RootCmd.execRootCmdSilent("rm -r ${filesDir.absolutePath}/private/${parent.name}")
                        RootCmd.execRootCmd("cp -r ${parent.absolutePath} ${filesDir.absolutePath}/private")
                        deleteDir(parent.absolutePath)
                    }
                }
            }
            else -> {
                builder.setTitle(R.string.strange)
                builder.setMessage(String.format(getString(R.string.unkonw),parent.absolutePath))
                builder.setPositiveButton(R.string.determine, null)
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
            builder.setTitle(R.string.restart_effect)
            builder.setMessage(R.string.restart_now)
            builder.setNegativeButton(R.string.later,null)
            builder.setPositiveButton(R.string.reboot) { _, _ ->
                RootCmd.execRootCmdSilent("reboot")
            }
            builder.show()
        }
    }
}