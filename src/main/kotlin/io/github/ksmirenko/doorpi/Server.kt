package io.github.ksmirenko.doorpi

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket


class Server: Runnable {
    private val port = 1489
    private val serverSocket: ServerSocket
    private var isStopped = false

    init {
        serverSocket = ServerSocket(port)
        println("Server running on port $port.")
    }

    override fun run() {
        try {
            while (!isStopped) {
                try {
                    val clientSocket = serverSocket.accept()
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
            serverSocket.close()
            println("Server stopped.")
        }
    }

    private fun handleClientSocket(clientSocket: Socket) {
        println("<<<<< Client ${clientSocket.inetAddress}:${clientSocket.port} connected.")
        clientSocket.getOutputStream().writer().use { outWriter ->
            clientSocket.getInputStream().reader().buffered().use { inReader ->
                var inputLine = inReader.readLine()
                while (inputLine != null) {
                    println(inputLine)

                    val outputLine = inputLine
                    outWriter.write("$outputLine\n")
                    outWriter.flush()

                    inputLine = inReader.readLine()
                }
            }
        }
        println(">>>>> Client ${clientSocket.inetAddress}:${clientSocket.port} disconnected.")
    }
}
