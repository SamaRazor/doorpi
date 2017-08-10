package io.github.ksmirenko.doorpi

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

fun main(args: Array<String>) {
    val server = Server()
    async(CommonPool) {
        server.run()
    }

    while (true) {
        val input = readLine()
        when (input) {
            "/close" -> {
                server.closeAll()
            }
            "/stop" -> {
                server.stop()
                return
            }
        }
    }
}
