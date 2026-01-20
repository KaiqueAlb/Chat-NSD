package com.example.chatnsd.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun Welcome(navController: NavHostController) {
    var text by remember { mutableStateOf("") }
    var warning by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bem vindo ao Chat NSD"
        )
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Digite seu nome") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Text(
            text = warning,
            modifier = Modifier.padding(16.dp),
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (text == "") {warning = "Digite um nome válido"
                } else navController.navigate("ChatServer/$text")
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Criar Sala")
        }
        Button(
            onClick = {
                if (text == "") {warning = "Digite um nome válido"
                } else navController.navigate("ConnectionScreen/$text")
            }
        ) {
            Text(text = "Conectar a Sala")
        }
    }
}