package io.github.ksmirenko.doorpi

import io.github.ksmirenko.doorpi.BicycleProtocol.State.*

class BicycleProtocol {
    private var state = State.IDLE
    private val headers = HashMap<String, String>()
    private var isBadRequest = false

    fun processInput(inputLine: String): String? {
        when (state) {
            IDLE -> when (inputLine) {
                "Bicycle/1.0" -> {
                    headers.clear()
                    isBadRequest = false
                    state = READING_REQ
                    return null
                }
                else -> return StatusCode.BAD_REQUEST.string
            }

            READING_REQ -> when (inputLine) {
                "End-Bicycle" -> {
                    state = IDLE
                    if (!isBadRequest && checkMap()) {
                        // TODO store the headers
                        println("Parsed request:\n$headers\n")

                        return StatusCode.OK.string
                    } else {
                        return StatusCode.BAD_REQUEST.string
                    }
                }

                else -> {
                    val parsedLine = inputLine.split(": ", ignoreCase = false, limit = 2)
                    if (parsedLine.size != 2) {
                        isBadRequest = true
                        return null
                    }
                    headers.put(parsedLine[0], parsedLine[1])
                    return null
                }
            }
        }
    }

    private fun checkMap(): Boolean {
        if (!requiredKeys.all { headers.containsKey(it) }) {
            return false
        }
        return when (headers["req-type"]) {
            null -> false
            in allowedReqTypes -> true
            else -> false
        }
    }

    companion object {
        private val requiredKeys = listOf("src", "dest", "time", "req-type")
        private val allowedReqTypes = listOf("auth", "dump")
    }

    private enum class State {
        IDLE, READING_REQ
    }

    private enum class StatusCode(val string: String) {
        OK("Bicycle/1.0 200"), BAD_REQUEST("Bicycle/1.0 400")
    }
}
