package com.erning.getapp.util

import android.util.Log
import java.io.File

/**
 * Created by 二宁 on 2018/5/29.
 */

fun File.deleteSelfAndSubFile(){
    Log.d("要删除的文件或目录",absolutePath)
    try {
        if (isDirectory){
            val files = listFiles()
            if (files == null || files.isEmpty()){
                delete()
            }else{
                files.forEach { it.deleteSelfAndSubFile() }
            }
        } else if(isFile){
            delete()
        }
    }catch (e:Exception){}
}