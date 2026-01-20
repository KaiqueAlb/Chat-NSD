package com.example.chatnsd

import android.app.Application
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.nsd.DiscoveryRequest
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatnsd.data.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class ConnectionManager(application: Application): AndroidViewModel(application) {
    private val SERVICE_TYPE: String = "_chat._tcp."
    private var isDiscoveryRunning = false
    private var firstMensage = true
    var clientName by mutableStateOf<String>("")
    private var socket: Socket? = null


    var devicesFound = mutableStateListOf<NsdServiceInfo>()
    var messageList = mutableStateListOf<Message>()

    val nsdManager = getApplication<Application>().applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            Log.d("NSD_LOG", "Serviço registrado com sucesso!")
            Log.d("NSD_LOG", "Nome: ${NsdServiceInfo.serviceName}")
            Log.d("NSD_LOG", "Tipo: ${NsdServiceInfo.serviceType}")
            Log.d("NSD_LOG", "Porta: ${NsdServiceInfo.port}")

        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e("NSD_LOG", "Falha no registro: $errorCode")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
    }

    fun InitServerSocket(nome: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverSocket = ServerSocket(0)
                val serverPort: Int = serverSocket.localPort

                val serviceInfo = NsdServiceInfo()
                serviceInfo.serviceName = "ChatNSD: $nome"
                serviceInfo.serviceType = SERVICE_TYPE
                serviceInfo.port = serverPort


                nsdManager.registerService(
                    serviceInfo,
                    NsdManager.PROTOCOL_DNS_SD,
                    registrationListener
                )

                Log.d("NSD_LOG", "Servidor iniciado na porta $serverPort")

                socket = serverSocket.accept()

                val saida = PrintWriter(socket?.getOutputStream(),true)
                saida.println(nome)

                mensageManager(socket)

            } catch (e: Exception) {
                e.printStackTrace()
            }finally {
                socket?.close()
            }
        }
    }

    fun discoveryService() {
        devicesFound.clear()
        if (isDiscoveryRunning) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.d("NSD_LOG", "Erro ao parar busca anterior (ignorado): ${e.message}")
            }
            isDiscoveryRunning = false
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            isDiscoveryRunning = true
        } catch (e: Exception) {
            Log.e("NSD_LOG", "Falha ao iniciar descoberta: ${e.message}")
            isDiscoveryRunning = false
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String?) {
            isDiscoveryRunning = true
            Log.d("NSD_LOG", "Busca iniciada")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            isDiscoveryRunning = false

            Log.i("NSD_LOG", "Busca parada: $serviceType")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.d("NSD_LOG", "Serviço encontrado: ${serviceInfo?.serviceName}")
            when {
                serviceInfo?.serviceType != SERVICE_TYPE -> return

                serviceInfo.serviceName.contains("ChatNSD") -> devicesFound.add(serviceInfo)
            }
            Log.d("NSD_LOG", "Dispositivos encontrados: ${devicesFound.map{it.serviceName}}")
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.e("NSD_LOG", "Serviço perdido: ${serviceInfo?.serviceName}")
        }

        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e("NSD_ERROR", "Discovery falhou: $errorCode")
            isDiscoveryRunning = false
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e("NSD_LOG", "Falha na parada da busca: $errorCode")
            isDiscoveryRunning = false
        }
    }

    fun connectDevice(serviceInfo: NsdServiceInfo, name: String?) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.e("NSD_LOG", "Falha no resolve: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        Log.d("NSD_LOG", "Serviço resolvido: ${serviceInfo.serviceName}")

                        val host = serviceInfo.host
                        val port: Int = serviceInfo.port

                        socket = Socket(host, port)
                        val saida = PrintWriter(socket?.getOutputStream(),true)

                        saida.println(name)

                        Log.d("NSD_LOG", "Conexão estabelecida com ${serviceInfo.serviceName}, Port: $port, Host: $host")

                        mensageManager(socket)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        nsdManager.resolveService(serviceInfo, resolveListener)
    }

    fun client(name : String?){
        viewModelScope.launch {
            try {
                val saida = PrintWriter(socket?.getOutputStream(),true)
                saida.println(name)

                mensageManager(socket)
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun mensageManager(socket: Socket?){
        withContext(Dispatchers.IO){
            try {
                val entrada = BufferedReader(InputStreamReader(socket?.getInputStream()))

                while (socket?.isConnected == true && !socket.isClosed){
                    val readLine = entrada.readLine() ?: break
                    Log.d("NSD_LOG", "Mensagem recebida: $readLine")
                    if(firstMensage){
                        firstMensage = false
                        withContext(Dispatchers.Main){
                            clientName = readLine
                        }

                    }else{
                        withContext(Dispatchers.Main){
                            messageList.add(Message(readLine,false))
                        }
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sendMessage = PrintWriter(socket?.outputStream,true)
                sendMessage.println(message)

                withContext(Dispatchers.Main){
                    messageList.add(Message(message,true))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun testMSG(text: String){
        messageList.add(Message(text,true))
        messageList.add(Message("Olá",false))
    }
}

