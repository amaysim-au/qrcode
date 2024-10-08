package com.example.qrcode

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView

class QRCaptureView(registrar: PluginRegistry.Registrar?,
                    binding: ActivityPluginBinding?,
                    activity: Activity,
                    messenger: BinaryMessenger,
                    id: Int) :
        PlatformView, MethodCallHandler {

    var barcodeView: BarcodeView? = null
    var cameraPermissionContinuation: Runnable? = null
    var requestingPermission = false
    val channel: MethodChannel

    private val mActivity = activity

    init {
        registrar?.addRequestPermissionsResultListener(CameraRequestPermissionsListener())
        binding?.addRequestPermissionsResultListener(CameraRequestPermissionsListener())
        channel = MethodChannel(messenger, "plugins/qr_capture/method_$id")
        channel.setMethodCallHandler(this)
        checkAndRequestPermission(null)

        val barcode = BarcodeView(activity)
        this.barcodeView = barcode
        barcode.decodeContinuous(
            object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    channel.invokeMethod("onCaptured", result.text)
                }

                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
            }
        )

        barcode.resume()

        activity.application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityPaused(p0: Activity) {
                    if (p0 == activity) {
                        barcodeView?.pause()
                    }
                }

                override fun onActivityResumed(p0: Activity) {
                    if (p0 == activity) {
                        barcodeView?.resume()
                    }
                }

                override fun onActivityStarted(p0: Activity) {
                }

                override fun onActivityDestroyed(p0: Activity) {
                }

                override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
                }

                override fun onActivityStopped(p0: Activity) {
                }

                override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                }

            }
        )
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call?.method){
            "checkAndRequestPermission" -> {
                checkAndRequestPermission(result)
            }
        }

        when(call?.method){
            "resume" -> {
                resume()
            }
        }

        when(call?.method){
            "pause" -> {
                pause()
            }
        }
    }

    private fun resume() {
        barcodeView?.resume()
    }

    private fun pause() {
        barcodeView?.pause()
    }

    private fun checkAndRequestPermission(result: MethodChannel.Result?) {
        if (cameraPermissionContinuation != null) {
            result?.error("cameraPermission", "Camera permission request ongoing", null);
        }

        cameraPermissionContinuation = Runnable {
            cameraPermissionContinuation = null
            if (!hasCameraPermission()) {
                result?.error(
                        "cameraPermission", "MediaRecorderCamera permission not granted", null)
                return@Runnable
            }
        }

        requestingPermission = false
        if (hasCameraPermission()) {
            cameraPermissionContinuation?.run()
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestingPermission = true
                mActivity
                        .requestPermissions(
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_REQUEST_ID)
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                mActivity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun getView(): View {
        return this.barcodeView!!;
    }

    override fun dispose() {
        barcodeView?.pause()
        barcodeView = null
    }

    companion object {
        const val CAMERA_REQUEST_ID = 513469796
    }

    private inner class CameraRequestPermissionsListener : PluginRegistry.RequestPermissionsResultListener {
        override fun onRequestPermissionsResult(id: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
            if (id == CAMERA_REQUEST_ID && grantResults[0] == PERMISSION_GRANTED) {
                cameraPermissionContinuation?.run()
                return true
            }
            return false
        }
    }
}
