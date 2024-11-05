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
        Thread {
            try {
                val serverSocket = ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"))
                Log.d("BrightnessService", "ServerSocket listening on port $port")
                while (true) {
                    val socket: Socket = serverSocket.accept()
                    handleClient(socket)
                }
            } catch (e: Exception) {
                Log.e("BrightnessService", "Error: ${e.message}")
                e.printStackTrace()
            }
        }.start()
        return START_STICKY
    }


    private fun handleClient(socket: Socket) {
        val input = socket.getInputStream().bufferedReader()
        val message = input.readLine() ?: return
        Log.d("brightServices", message)

        if (message.startsWith("LightsService|setBrightness|")) {
            val brightness = message.split("|")[2].toIntOrNull()
            brightness?.let {
                if (it in 0..255) { // 修改范围为 0-255
                    setBrightness(it)
                    socket.getOutputStream().write("$it\n".toByteArray())  // 回复亮度值
                    socket.getOutputStream().flush()  // 刷新输出流
                }
            }
        } else {
            socket.getOutputStream().write("asd".toByteArray())  // 非预期请求时回复"asd"
            Log.d("BrightnessService", "asd")
            socket.getOutputStream().flush()  // 刷新输出流
        }

        socket.close()  // 关闭连接
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
