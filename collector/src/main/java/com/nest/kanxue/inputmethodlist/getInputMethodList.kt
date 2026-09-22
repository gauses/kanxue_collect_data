package com.nest.kanxue.inputmethodlist

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodInfo
import org.json.JSONArray


object getInputMethodList {

    fun getInfo(context: Context):List<String> {

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val inputMethodList = inputMethodManager.enabledInputMethodList
        return inputMethodList.map {
            it.loadLabel(context.packageManager).toString()
        }

    }
}