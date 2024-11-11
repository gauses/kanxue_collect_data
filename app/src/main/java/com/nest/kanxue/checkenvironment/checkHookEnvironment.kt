package com.nest.kanxue.checkenvironment

import android.util.Log

object checkHookEnvironment {

    fun getInfo(){

        Log.d("checkfrida", "containsLinjectorInFd = " + checkfrida.containsLinjectorInFd())
        Log.d("checkfrida", "isFridaServerPresent = " + checkfrida.isFridaServerPresent())


        Log.d("sb" , "checkXposed check_xposed_file_path = "+ checkXposed.check_xposed_file_path())
        Log.d("sb" , "checkXposed check_Hook_loadClass = "+ checkXposed.check_Hook_loadClass(this::class.java.classLoader))
        Log.d("sb" , "checkXposed check_data_package = "+ checkXposed.check_data_package())

    }

}