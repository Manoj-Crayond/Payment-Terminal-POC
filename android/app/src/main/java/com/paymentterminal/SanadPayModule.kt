package com.paymentterminal

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.Callback

class SanadPayModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    
    override fun getName(): String {
        return "SanadPay"
    }

    private val packageName: String = reactApplicationContext.packageName
    private val packageNameClass: String = "${packageName}.MainActivity"

    private fun sendEvent(eventName: String, params: WritableMap?) {
    reactApplicationContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(eventName, params)
    }

    @ReactMethod
    fun sendBroadcastMessage(amount: String, transactionId: String, callback: Callback) {
        val context = reactApplicationContext
        val sendIntent = Intent("global.citytech.pos").apply {
            action = Intent.ACTION_SEND
            component = ComponentName("global.citytech.pos", "global.citytech.pos.ui.idle.IdleActivity")
            `package` = "global.citytech.pos"
            putExtra("QTransactionAmount", amount)
            putExtra("QTranID", transactionId)
            putExtra("QPackageName", packageName)
            putExtra("QPackageClass", packageNameClass)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val params = Arguments.createMap()
        if (context.packageManager.resolveActivity(sendIntent, 0) != null) {
            context.startActivity(sendIntent)
            
            params.putString("message", "Broadcast Sent!")
            params.putBoolean("success", true)

            callback.invoke(params)
        } else {
            params.putString("message", "Failed to send broadcast, no package found.")
            params.putBoolean("success", false)

            callback.invoke(params)
        }
    }

    @SuppressLint("SetTextI18n")
    @ReactMethod
    fun receivingData() {
        val intent = currentActivity?.intent;
        if (intent != null) {
            val params = Arguments.createMap()

            params.putString("RTransactionAmount", intent.getStringExtra("RTransactionAmount") ?: "");
            params.putString("RTransactionStatusCode", intent.getStringExtra("RTransactionStatusCode") ?: "");
            params.putString("RTransactionStatusDescription", intent.getStringExtra("RTransactionStatusDescription") ?: "");
            params.putString("RAuthCode", intent.getStringExtra("RAuthCode") ?: "");
            params.putString("RDate", intent.getStringExtra("RDate") ?: "");
            params.putString("RCardNo", intent.getStringExtra("RCardNo") ?: "");
            params.putString("RTerminalID", intent.getStringExtra("RTerminalID") ?: "");

            val statusDescription = intent.getStringExtra("RTransactionStatusDescription") ?: "No description available"
            Log.d("SanadPayModule", "RTransactionStatusDescription: $statusDescription")
            Toast.makeText(reactApplicationContext, "Status: $statusDescription", Toast.LENGTH_LONG).show()
            
            sendEvent("ReceivingData", params)
        } else {
            Log.d("SanadPayModule", "Intent is null")
            Toast.makeText(reactApplicationContext, "No data received from intent", Toast.LENGTH_SHORT).show()
            sendEvent("ReceivingData", null)
        }
    }
}
