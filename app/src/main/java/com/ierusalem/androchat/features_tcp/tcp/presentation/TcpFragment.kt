package com.ierusalem.androchat.features_tcp.tcp.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.ierusalem.androchat.features_tcp.tcp.TcpScreenNavigation
import com.ierusalem.androchat.features_tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.domain.ClientStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.ServerStatus
import com.ierusalem.androchat.features_tcp.tcp.domain.TcpViewModel
import com.ierusalem.androchat.features_tcp.tcp.presentation.components.rememberAllTabs
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.executeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.InternalAPI
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@AndroidEntryPoint
class TcpFragment : Fragment() {

    private val viewModel: TcpViewModel by viewModels()

    private lateinit var serverSelectorManager: SelectorManager
    private lateinit var clientSelectorManager: SelectorManager
    private lateinit var clientSocket: Socket
    private lateinit var serverSocket: ServerSocket

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        return ComposeView(requireContext()).apply {
            setContent {
                val scope = rememberCoroutineScope()
                val allTabs = rememberAllTabs()
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0F,
                    pageCount = { allTabs.size },
                )

                val handleTabSelected by rememberUpdatedState { tab: TcpView ->
                    // Click fires the index to update
                    // The index updating is caught by the snapshot flow
                    // Which then triggers the page update function
                    val index = allTabs.indexOf(tab)
                    scope.launch(context = Dispatchers.Main) {
                        pagerState.animateScrollToPage(
                            index
                        )
                    }
                }

                val state by viewModel.state.collectAsStateWithLifecycle()

                AndroChatTheme {
                    TcpScreen(
                        eventHandler = {
                            viewModel.handleEvents(it)
                        },
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabChanged = { handleTabSelected(it) },
                        state = state
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.screenNavigation.executeWithLifecycle(
            lifecycle = viewLifecycleOwner.lifecycle,
            action = ::executeNavigation
        )
    }

    private fun executeNavigation(navigation: TcpScreenNavigation) {
        when (navigation) {
            TcpScreenNavigation.OnNavIconClick -> findNavController().popBackStack()

            TcpScreenNavigation.OnSettingsClick -> {

            }

            TcpScreenNavigation.OnCloseServerClick -> {
                serverSocket.close()
                viewModel.updateHotspotTitleStatus(ServerStatus.Idle)
                Log.d(
                    "ahi3646",
                    "Closing Server  at ${serverSocket.localAddress} - ${serverSocket.isClosed} - ${serverSocket.socketContext.isActive} "
                )
            }

            TcpScreenNavigation.OnDisconnectServerClick -> {
                clientSocket.close()
                viewModel.updateClientTitleStatus(ClientStatus.Idle)
            }

            is TcpScreenNavigation.OnCreateServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    openHotspot(
                        hotspotName = navigation.hotspotName,
                        hotspotPassword = navigation.hotspotPassword,
                        port = navigation.portNumber
                    )
                }
            }

            is TcpScreenNavigation.OnConnectToServerClick -> {
                CoroutineScope(Dispatchers.Default).launch {
                    connectToServer()
                }
//                runBlocking {
//                    connectToServer()
//                }
            }
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun connectToServer() {
//        runBlocking {
            clientSelectorManager = SelectorManager(Dispatchers.IO)
            clientSocket = aSocket(clientSelectorManager).tcp().connect("127.0.0.1", 9002)
            Log.d("ahi3646", "connectToServer ip address: ${clientSocket.localAddress} ")

            val receiveChannel = clientSocket.openReadChannel()
            val sendChannel = clientSocket.openWriteChannel(autoFlush = true)

//            launch(Dispatchers.IO) {
//                val receiveChannel = clientSocket.openReadChannel()
//                val sendChannel = clientSocket.openWriteChannel(autoFlush = true)
//                sendChannel.writeFully("hello\r\n".toByteArray())
//                println("Server said: '${receiveChannel.readUTF8Line()}'")
//            }

            withContext(Dispatchers.IO) {
                while (true) {
                    val greeting = receiveChannel.readUTF8Line()
                    if (greeting != null) {
                        Log.d("ahi3646", greeting)
                    } else {
                        Log.d("ahi3646", "Server closed a connection")
                        clientSocket.close()
                        clientSelectorManager.close()
                        exitProcess(0)
                    }
                }
            }
//
//            while (true) {
//                val myMessage = "my message"
//                sendChannel.writeStringUtf8("$myMessage\n")
//            }
//        }

    }

    @OptIn(InternalAPI::class)
    private suspend fun openHotspot(hotspotName: String, hotspotPassword: String, port: Int) {
        Log.d(
            "ahi3646",
            "openHotspot: " +
                    "\nhotspotName - $hotspotName" +
                    "\nhotspotPassword - $hotspotPassword" +
                    "\nport - $port"
        )

        runBlocking {
            //Create a server socket
            serverSelectorManager = SelectorManager(Dispatchers.IO)
//        serverSelectorManager =
//            SelectorManager(Executors.newCachedThreadPool().asCoroutineDispatcher())
            serverSocket = aSocket(serverSelectorManager)
                .tcp()
                //.configure {
                //todo think about these
                //reuseAddress = true
                //reusePort = true
                //}
                .bind("127.0.0.1", 9002)


            Log.d("ahi3646", "Server is listening at ${serverSocket.localAddress}")
            viewModel.updateHotspotTitleStatus(ServerStatus.Created)

            while (true) {
                //Accept incoming connections
                val socket = serverSocket.accept()
                Log.d("ahi3646", "Socket Accepted $socket")


//            val input = socket.openReadChannel()
//            val output = socket.openWriteChannel(autoFlush = true)
//            val line = input.readUTF8Line()
//
//            println("Server received '$line' from ${socket.remoteAddress}")
//            output.writeFully("$line back\r\n".toByteArray())
//
                launch {
                    //Receive data
                    val receiveChannel = socket.openReadChannel()
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    sendChannel.writeStringUtf8("Please enter your name\n")
//                    try {
////                        while (true) {
//                        val name = receiveChannel.readUTF8Line()
//                        sendChannel.writeStringUtf8("Hello, $name!\n")
////                        }
//                    } catch (e: Throwable) {
//                        Log.d("ahi3646", "socket closed ")
//                        socket.close()
//                    }
                }
            }
        }
    }

}