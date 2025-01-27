package com.paymentterminal

import android.content.Intent
import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.ReactContext
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

    override fun getMainComponentName(): String = "PaymentTerminal"

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent for the current activity
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        if (intent.action == Intent.ACTION_SEND) {
            callReceivingData()  // Pass intent to receivingData
        }
    }

    private fun callReceivingData() {
        val reactInstanceManager: ReactInstanceManager = (application as MainApplication).reactNativeHost.reactInstanceManager

        // Check if ReactContext is initialized
        val reactContext: ReactContext? = reactInstanceManager.currentReactContext

        if (reactContext != null) {
            // ReactContext is available, call the method directly
            val sanadPayModule = reactContext.getNativeModule(SanadPayModule::class.java)
            sanadPayModule?.receivingData()  // Pass the intent to receivingData
        } else {
            // ReactContext is not yet initialized, set a listener
            reactInstanceManager.addReactInstanceEventListener(object : ReactInstanceManager.ReactInstanceEventListener {
                override fun onReactContextInitialized(initializedContext: ReactContext) {
                    val sanadPayModule = initializedContext.getNativeModule(SanadPayModule::class.java)
                    sanadPayModule?.receivingData()  // Pass the intent to receivingData

                    // Remove listener after it's called
                    reactInstanceManager.removeReactInstanceEventListener(this)
                }
            })
        }
    }
}
