package com.example.chatnsd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatnsd.ui.theme.ChatNSDTheme
import com.example.chatnsd.view.ChatScreen
import com.example.chatnsd.view.ConnectionScreen
import com.example.chatnsd.view.Welcome

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatNSDTheme {
                val connectionManager: ConnectionManager = viewModel(factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application))
                val navController: NavHostController = rememberNavController()
                NavHost(navController = navController, startDestination = "Welcome"){
                    composable(route = "Welcome"){ Welcome(navController) }
                    composable(route = "ChatServer/{name}") {
                        val name = it.arguments?.getString("name")
                        ChatScreen(navController,connectionManager,name,true)
                    }
                    composable(route = "ChatClient/{name}") {
                        val name = it.arguments?.getString("name")
                        ChatScreen(navController,connectionManager,name,false)
                    }
                    composable(route="ConnectionScreen/{name}") {
                        val name = it.arguments?.getString("name")
                        ConnectionScreen(navController,connectionManager,name)
                    }
                }
            }
        }
    }
}