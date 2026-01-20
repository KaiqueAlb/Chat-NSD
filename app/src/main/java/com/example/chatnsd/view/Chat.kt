package com.example.chatnsd.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.chatnsd.ConnectionManager
import com.example.chatnsd.data.Message

@Composable
fun ChatScreen(navController: NavHostController,connectionManager: ConnectionManager ,name: String?,isServer: Boolean) {
    if(isServer) connectionManager.InitServerSocket(name)
    else connectionManager.client(name)

    Column {
        StatusBar(connectionManager)
        LazyColumn(
            modifier = Modifier.weight(1f)
                .fillMaxSize()
        ){
            items(connectionManager.messageList){msg ->
                MensageBallon(msg)
            }
        }
        SendInput(connectionManager)
    }

}

@SuppressLint("UnrememberedMutableState")
@Composable
fun StatusBar(connectionManager: ConnectionManager){

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if(connectionManager.clientName == "") Color.LightGray else Color(0xFFD1FFD1)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(if (connectionManager.clientName == "") Color.Red else Color.Green)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (connectionManager.clientName == "")
                    "Nenhum dispositivo conectado"
                else
                    "Conectado com ${connectionManager.clientName}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SendInput(connectionManager: ConnectionManager){
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Digite sua mensagem") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )

            Button(
                onClick = {
                    if (text.isNotEmpty()){
                        connectionManager.sendMessage(text)
                        text = ""
                    }

                },
                modifier = Modifier.padding(16.dp),
                enabled = text.isNotEmpty()
            ) {
                Text(
                    text = "Enviar",
                )
            }
        }
    }
}

@Composable
fun MensageBallon(msg: Message){
    val alinhamento = if (msg.isMine) Alignment.CenterEnd else Alignment.CenterStart
    val corFundo = if (msg.isMine) Color.Cyan else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alinhamento
    ) {
        Surface(
            color = corFundo,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (msg.isMine) 12.dp else 0.dp,
                bottomEnd = if (msg.isMine) 0.dp else 12.dp
            ),
            shadowElevation = 2.dp
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}