package com.example.demo1

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.provider.Settings
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class BrightnessService : Service() {
    private val port = 18099

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BrightnessService", "Service started")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"))
                Log.d("BrightnessService", "ServerSocket listening on port $port")
                while (true) {
                    val socket: Socket = serverSocket.accept()
                    Log.d("BrightnessService", "New connection accepted")
                    // 使用协程处理每个连接
                    launch { handleClient(socket) }
                }
            } catch (e: Exception) {
                Log.e("BrightnessService", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    private suspend fun handleClient(socket: Socket) {
        Log.d("BrightnessService", "Handling new client")

        val buffer = ByteArray(1024)
        val bytesRead = socket.getInputStream().read(buffer)
        if (bytesRead != -1) {
            val message = String(buffer, 0, bytesRead)
            Log.d("BrightnessService", "Received message: $message")

                if (message.startsWith("LightsService|setBrightness|")) {
                val brightness = message.split("|")[2].toIntOrNull()
                brightness?.let {
                    if (it in 0..255) {
                        setBrightness(it)
                        socket.getOutputStream().write("$it\n".toByteArray())
                        socket.getOutputStream().flush()
                    }
                }
            } else {
                socket.getOutputStream().write("asd".toByteArray())
                socket.getOutputStream().flush()
            }
        }
        socket.close()
    }



    private fun setBrightness(brightness: Int) {

        // 确保应用具有写入设置的权限
        try {
            val contentResolver: ContentResolver = contentResolver
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
