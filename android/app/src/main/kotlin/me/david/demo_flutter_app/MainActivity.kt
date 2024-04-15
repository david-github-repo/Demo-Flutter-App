package me.david.demo_flutter_app

import android.os.BatteryManager
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumSet

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger, "demo_flutter_app.david.me/helloworld"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "printHelloWorld" -> {
                    result.success("Hello, world from native Kotlin code!")
                }

                "readFile" -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val fileContents = readFile()
                        result.success(fileContents)
                    }
                }

                else -> {
                    result.notImplemented()
                }
            }
        }

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger, "demo_flutter_app.david.me/battery"
        ).setMethodCallHandler {
            // This method is invoked on the main thread.
                call, result ->
            if (call.method == "getBatteryLevel") {
                val batteryLevel = getBatteryLevel()

                if (batteryLevel != -1) {
                    result.success(batteryLevel)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        return batteryLevel
    }

    private suspend fun readFile() = withContext(Dispatchers.IO) {
        val client = SMBClient()

        client.connect("192.168.0.82").use { connection ->
            val ac = AuthenticationContext("", CharArray(0), null)
            val session = connection.authenticate(ac)

            (session.connectShare("Sharing") as DiskShare).use { share ->
                val file = share.openFile(
                    "Hello.txt",
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null
                )

                println(file)
            }
        }
    }
}