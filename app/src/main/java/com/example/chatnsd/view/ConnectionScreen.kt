package com.example.chatnsd.view

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatnsd.ConnectionManager

@Composable
fun ConnectionScreen(navController: NavHostController,connectionManager: ConnectionManager ,name: String?){
    var status by remember{mutableStateOf("Nenhum dispositivo encontrado!")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(0.dp,24.dp,0.dp,0.dp),
            text = "Dispositivos Encontrados:",
            fontSize = 24.sp
        )

        Button(
            onClick = {
                connectionManager.discoveryService()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Buscar")
        }

        Text(
            text = status,
            modifier = Modifier.padding(16.dp),
            color = Color.Red
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ){
            items(connectionManager.devicesFound){device ->
                status = if (connectionManager.devicesFound.isNotEmpty()) ""
                else "Nenhum dispositivo encontrado!"

                DeviceButton(device){
                    connectionManager.connectDevice(device,name)
                    navController.navigate("ChatClient/$name")
                }
            }
        }
    }

}

@Composable
fun DeviceButton(device: NsdServiceInfo,onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = device.serviceName)
    }
}