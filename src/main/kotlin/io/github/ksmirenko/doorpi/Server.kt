package io.github.ksmirenko.doorpi

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class Server : Runnable {
    private val port = 2507
    private val serverSocket: ServerSocket

    private val clientSockets = HashSet<Socket>()
    private var isStopped = false

    init {
        serverSocket = ServerSocket(port)
        println("----- Server running on port $port.")
    }

    override fun run() {
        try {
            while (!isStopped) {
                try {
                    val clientSocket = serverSocket.accept()
                    synchronized(clientSockets) {
                        clientSockets.add(clientSocket)
                    }
                    async(CommonPool) {
                        handleClientSocket(clientSocket)
                    }
                } catch (e: IOException) {
                    if (isStopped) {
                        return
                    }
                    e.printStackTrace()
                }
            }
        } finally {
            stop()
        }
    }

    fun closeAll() {
        synchronized(clientSockets) {
            val iterator = clientSockets.iterator()
            while (iterator.hasNext()) {
                val curSocket = iterator.next()
                println(">>>>> Disconnecting ${curSocket.inetAddress}:${curSocket.port}")
                curSocket.close()
                iterator.remove()
            }
        }
        println("----- All current connections were closed.")
    }

    fun stop() {
        if (!isStopped) {
            closeAll()
            serverSocket.close()
            isStopped = true
            println("----- Server stopped.")
        }
    }

    private fun handleClientSocket(clientSocket: Socket) {
        val socketInfo = "${clientSocket.inetAddress}:${clientSocket.port}"
        println("<<<<< Client $socketInfo connected.")

        clientSocket.getOutputStream().writer().use { outWriter ->
            clientSocket.getInputStream().reader().buffered().use { inReader ->
                val protocol = BicycleProtocol()

                var inputLine = inReader.readLine()
                while (inputLine != null) {
                    println("<<<<< From $socketInfo: $inputLine")

                    val response = protocol.processInput(inputLine)
                    response?.let {
                        println(">>>>> To $socketInfo: $response")
                        outWriter.write("$response\n")
                        outWriter.flush()
                    }

                    inputLine = inReader.readLine()
                }
            }
        }
        clientSockets.remove(clientSocket)
        println("<<<<< Client $socketInfo disconnected.")
    }
}
