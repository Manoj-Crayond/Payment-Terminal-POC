package com.paymentterminal

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class SanadPayModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext),
    LifecycleEventListener {

    override fun getName(): String {
        return "SanadPay"
    }

    private val packageName: String = reactApplicationContext.packageName
    private val packageNameClass: String = "com.paymentterminal.MainActivity"

    init {
        reactContext.addLifecycleEventListener(this)
    }

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
        val currentActivity = currentActivity
        if (currentActivity != null) {
            val intent = currentActivity.intent
            if (intent != null) {
                val RTransactionAmount = intent.getStringExtra("RTransactionAmount") ?: ""
                val RTransactionStatusCode = intent.getStringExtra("RTransactionStatusCode") ?: ""
                val RTransactionStatusDescription = intent.getStringExtra("RTransactionStatusDescription") ?: "No description available"
                val RAuthCode = intent.getStringExtra("RAuthCode") ?: ""
                val RDate = intent.getStringExtra("RDate") ?: ""
                val RCardNo = intent.getStringExtra("RCardNo") ?: ""
                val RTerminalID = intent.getStringExtra("RTerminalID") ?: ""
                val RCardScheme = intent.getStringExtra("RCardScheme") ?: ""
                val R_RRN = intent.getStringExtra("R_RRN") ?: ""

                // Log all received values
                Log.d("SanadPayModule", "RTransactionAmount=$RTransactionAmount")
                Log.d("SanadPayModule", "RTransactionStatusCode=$RTransactionStatusCode")
                Log.d("SanadPayModule", "RTransactionStatusDescription=$RTransactionStatusDescription")
                Log.d("SanadPayModule", "RAuthCode=$RAuthCode")
                Log.d("SanadPayModule", "RDate=$RDate")
                Log.d("SanadPayModule", "RCardNo=$RCardNo")
                Log.d("SanadPayModule", "RTerminalID=$RTerminalID")
                Log.d("SanadPayModule", "RCardScheme=$RCardScheme")
                Log.d("SanadPayModule", "R_RRN=$R_RRN")

                // Show a Toast with the status description
                Toast.makeText(reactApplicationContext, "Status: $RTransactionStatusDescription", Toast.LENGTH_LONG).show()

                // Create a map of parameters to send to React Native
                val params = Arguments.createMap().apply {
                    putString("RTransactionAmount", RTransactionAmount)
                    putString("RTransactionStatusCode", RTransactionStatusCode)
                    putString("RTransactionStatusDescription", RTransactionStatusDescription)
                    putString("RAuthCode", RAuthCode)
                    putString("RDate", RDate)
                    putString("RCardNo", RCardNo)
                    putString("RTerminalID", RTerminalID)
                    putString("RCardScheme", RCardScheme)
                    putString("R_RRN", R_RRN)
                }

                // Send the event to JavaScript
                sendEvent("ReceivingData", params)
            } else {
                // Log and notify when the intent is null
                Log.d("SanadPayModule", "Intent is null")
                Toast.makeText(reactApplicationContext, "No data received from intent", Toast.LENGTH_SHORT).show()
                sendEvent("ReceivingData", null)
            }
        } else {
            // Handle the case where the current activity is null
            Log.d("SanadPayModule", "Current activity is null")
            Toast.makeText(reactApplicationContext, "No activity context available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onHostResume() {
        Log.d("SanadPayModule", "onHostResume called")
        receivingData()
    }

    override fun onHostPause() {
        Log.d("SanadPayModule", "onHostPause called")
    }

    override fun onHostDestroy() {
        Log.d("SanadPayModule", "onHostDestroy called")
    }
}
