package com.example.demo1

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.demo1.ui.theme.Demo1Theme
import java.io.PrintWriter
import java.net.Socket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查并请求 WRITE_SETTINGS 权限
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, 99)
        } else {
            // 权限已被授予，继续进行其他初始化
        }

        // 启动亮度服务
        startService(Intent(this@MainActivity, BrightnessService::class.java))

        enableEdgeToEdge()
        setContent {
            Demo1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp), // 添加一些内边距
                        verticalArrangement = Arrangement.Center, // 垂直居中
                        horizontalAlignment = Alignment.CenterHorizontally // 水平居中
                    ) {
                        var brightnessValue by remember { mutableStateOf("") }

                        TextField(
                            value = brightnessValue,
                            onValueChange = {
                                if (it.isEmpty() || (it.toIntOrNull() in 0..250)) {
                                    brightnessValue = it
                                }
                            },
                            label = { Text("设置亮度 (60-250)") },
                            modifier = Modifier.fillMaxWidth() // 填满宽度
                        )

                        Spacer(modifier = Modifier.height(16.dp)) // 添加间距

                        Button(onClick = {
                            val brightness = brightnessValue.toIntOrNull()
                            if (brightness != null && brightness in 0..250) {


                                // 通过 Socket 发送亮度设置命令
//                                setBrightness(brightness)
                                sendBrightnessSetting(brightness)

                            } else {
                                Toast.makeText(this@MainActivity, "请输入有效的亮度值（60-250）", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("设置亮度")
                        }

                        Spacer(modifier = Modifier.height(32.dp)) // 添加间距

                        Greeting(name = "Android")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99) {
            if (Settings.System.canWrite(this)) {
                // 权限已授予，继续进行其他初始化
                Toast.makeText(this,"已授予",Toast.LENGTH_SHORT).show();

            } else {
                // 权限未授予，显示提示或进行其他处理
                Toast.makeText(this,"失败",Toast.LENGTH_SHORT).show();
                // 例如，可以显示一个 Toast 提示用户需要授权
            }
        }
    }

    private fun sendBrightnessSetting(brightness: Int) {
        Thread {
            try {
                // 连接到本地服务器
                val socket = Socket("localhost", 18099)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println("LightsService|setBrightness|$brightness")
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Demo1Theme {
        Greeting("Android")
    }
}
