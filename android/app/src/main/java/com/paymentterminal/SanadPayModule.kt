package com.paymentterminal

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class SanadPayModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

    override fun getName(): String {
        return "SanadPay"
    }

    private val packageName: String = reactApplicationContext.packageName
    private val sanadPayPackageName: String = "global.citytech.pos"

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
        try {
            val context = reactApplicationContext
            val sendIntent = Intent(sanadPayPackageName).apply {
                action = Intent.ACTION_SEND
                component = ComponentName(
                    sanadPayPackageName,
                    "${sanadPayPackageName}.ui.idle.IdleActivity"
                )
                `package` = sanadPayPackageName

                putExtra("QTransactionAmount", amount)
                putExtra("QTranID", transactionId)
                putExtra("QPackageName", packageName)
                putExtra("QPackageClass", "${packageName}.MainActivity")
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val params = Arguments.createMap()
            if (context.packageManager.resolveActivity(sendIntent, 0) != null) {
                context.startActivity(sendIntent)

                params.putString("message", "Payment processed successfully")
                params.putBoolean("success", true)

                callback.invoke(params)
            } else {
                params.putString("message", "Failed to send broadcast, no package found.")
                params.putBoolean("success", false)

                Toast.makeText(reactApplicationContext, "Failed to send broadcast, no package found.", Toast.LENGTH_SHORT).show()

                callback.invoke(params)
            }

        } catch (e: Exception) {
            val params = Arguments.createMap()
            params.putBoolean("success", false)
            params.putString("message", e.message);

            callback.invoke(params)
        }
    }

    @SuppressLint("SetTextI18n")
    @ReactMethod
    fun receivingData() {
        val activity = currentActivity
        if (activity == null) {
            Log.d("SanadPayModule", "receivingData: getCurrentActivity() returned null")
            return
        }

        val intent = activity.intent

        if (intent.action == Intent.ACTION_SEND) {

            val jsIntent = Arguments.createMap()
            jsIntent.putString("action", intent.action)
            jsIntent.putString("RTransactionAmount", intent.getStringExtra("RTransactionAmount"))
            jsIntent.putString("RTransactionStatusCode", intent.getStringExtra("RTransactionStatusCode"))
            jsIntent.putString("RTransactionStatusDescription", intent.getStringExtra("RTransactionStatusDescription"))
            jsIntent.putString("RAuthCode", intent.getStringExtra("RAuthCode"))
            jsIntent.putString("RDate", intent.getStringExtra("RDate"))
            jsIntent.putString("RCardNo", intent.getStringExtra("RCardNo"))
            jsIntent.putString("RTerminalID", intent.getStringExtra("RTerminalID"))
            jsIntent.putString("RCardScheme", intent.getStringExtra("RCardScheme"))
            jsIntent.putString("R_RRN", intent.getStringExtra("R_RRN"))

            sendEvent("sanadpay-receive", jsIntent)

            Toast.makeText(reactApplicationContext, "${intent.getStringExtra("RTransactionStatusDescription")}", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("SanadPayModule", "Received Intent is not ACTION_SEND, action=${intent.action}")
        }
    }

    override fun onHostResume() {
        receivingData()
        Log.d("SanadPayModule", "onHostResume called")
    }

    override fun onHostDestroy() {
        Log.d("SanadPayModule", "onHostDestroy called")
    }

    override fun onHostPause() {
        Log.d("SanadPayModule", "onHostPause called")
    }
}
