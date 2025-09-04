package com.swings.vitamatrix.utils

import android.util.Log
import android.view.Gravity
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import com.swings.vitamatrix.R
import java.util.ArrayDeque

enum class ToastType { ERROR, WARN, INFO, SUCCESS }

class Util {
    companion object {
        fun log(e: Exception) {
            Log.e("vita", "error: " + e.message, e)
        }

        fun log(message: String?) {
            Log.e("vita", "" + message)
        }

        fun toast(type: ToastType, message: String?) {
            if (message == null)
                return
            val params = ToastParams()
            params.text = message

            when (type) {
                ToastType.INFO -> params.style = CustomToastStyle(R.layout.toast_info)
                ToastType.ERROR -> params.style = CustomToastStyle(R.layout.toast_error)
                ToastType.WARN -> params.style = CustomToastStyle(R.layout.toast_warn)
                ToastType.SUCCESS -> params.style = CustomToastStyle(R.layout.toast_success)
            }
            Toaster.setGravity(Gravity.BOTTOM)
            Toaster.show(params)
        }




        private const val TAG = "ByteArrayLogger"


        fun byteArrayToHex(byteArray: ByteArray): String {
            return byteArray.joinToString("") { "%02x".format(it) }
        }

        fun log(byteArray: ByteArray) {
            val hexString = byteArrayToHex(byteArray)
            Log.e("logByteArray", "ByteArray (Hex): $hexString")
        }



        fun logByteArrayAsString(byteArray: ByteArray, tag: String = TAG) {
            val stringRepresentation = String(byteArray)
            Log.e(tag, "ByteArray (String): $stringRepresentation")
        }

        fun log(datas: ArrayDeque<ByteArray?>?) {
            val newData = datas?.flatMap { it?.toList() ?: emptyList() }?.toByteArray() ?: byteArrayOf()
            log(newData)
        }


    }
}