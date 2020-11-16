package com.erning.getapp.ui

import android.content.pm.PackageInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.erning.getapp.R
import com.erning.getapp.bean.BackupBean
import com.erning.getapp.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_backup.*
import org.jetbrains.anko.toast
import java.io.File

class BackupActivity : BaseActivity() {
    private val fileList = ArrayList<BackupBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getBackup()
        list_backup.adapter = Adapter()
    }

    private fun getBackup(){
        add(File("${filesDir.absolutePath}/user"))
        add(File("${filesDir.absolutePath}/system"))
        add(File("${filesDir.absolutePath}/private"))
        if (fileList.isEmpty())
            toast("经费不足 研发暂停")
    }

    private fun add(file:File){
        if (file.exists()) {
            file.listFiles().forEach { dir ->
                Log.d("发现文件",dir.absolutePath)
                dir.listFiles().forEach {
                    if (it.isFile && (it.name=="base.apk" || it.name=="${dir.name}.apk")){
                        //这是一个apk
                        val packageInfo = packageManager.getPackageArchiveInfo(it.absolutePath, 0)
                        if (packageInfo != null){
                            val back = BackupBean(dir)
                            back.appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
                            back.apk = it
                            back.appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                            back.appPackageName = packageInfo.applicationInfo.packageName
                            fileList.add(back)
                        }
                    }
                }
            }
        }
    }

    inner class Adapter:BaseAdapter(){
        override fun getItem(position: Int) = fileList[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getCount() = fileList.size
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item,parent,false)
            val icon = view.findViewById<ImageView>(R.id.icon)
            val name = view.findViewById<TextView>(R.id.name)
            val pack = view.findViewById<TextView>(R.id.pack)
            val size = view.findViewById<TextView>(R.id.size)
            val item = fileList[position]

            size.text = item.appSizeFormat
            icon.setImageDrawable(item.appIcon)
            name.text = item.appName
            pack.text = item.appPackageName

            return view
        }
    }

    private fun getPackageArchiveInfo(archiveFilePath: String, flags: Int): PackageInfo? {
        try {
            val packageParserClass = Class.forName("android.content.pm.PackageParser")
            val innerClasses = packageParserClass.declaredClasses
            val packageParserPackageClass: Class<*>? = innerClasses.firstOrNull { 0 == it.name.compareTo("android.content.pm.PackageParser\$Package") }
            val packageParserConstructor = packageParserClass.getConstructor(String::class.java)
            val parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File::class.java, String::class.java, DisplayMetrics::class.java, Int::class.javaPrimitiveType)
            val collectCertificatesMethod = packageParserClass.getDeclaredMethod("collectCertificates", packageParserPackageClass, Int::class.javaPrimitiveType)
            val generatePackageInfoMethod = packageParserClass.getDeclaredMethod("generatePackageInfo", packageParserPackageClass, IntArray::class.java, Int::class.javaPrimitiveType, Long::class.javaPrimitiveType, Long::class.javaPrimitiveType)
            packageParserConstructor.isAccessible = true
            parsePackageMethod.isAccessible = true
            collectCertificatesMethod.isAccessible = true
            generatePackageInfoMethod.isAccessible = true

            val packageParser = packageParserConstructor.newInstance(archiveFilePath)

            val metrics = DisplayMetrics()
            metrics.setToDefaults()

            val sourceFile = File(archiveFilePath)

            val pkg = parsePackageMethod.invoke(packageParser, sourceFile, archiveFilePath, metrics, 0) ?: return null

            if (flags and android.content.pm.PackageManager.GET_SIGNATURES != 0) {
                collectCertificatesMethod.invoke(packageParser, pkg, 0)
            }

            return generatePackageInfoMethod.invoke(null, pkg, null, flags, 0, 0) as PackageInfo
        } catch (e: Exception) {
            Log.e("Signature Monitor", "android.content.pm.PackageParser reflection failed: " + e.toString())
        }

        return null
    }
}
