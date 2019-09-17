package com.example.dan1ar.preludia.connecting

import android.os.Build
import android.support.annotation.RequiresApi
import android.system.ErrnoException
import java.net.InetAddress
import java.net.Socket

class Connecting
{
    // 10.42.0.1 159.69.151.175
    private val serverIP = "10.42.0.1"
    private val serverPort = 9673
    private val serverAdr = InetAddress.getByName(serverIP)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val socket = try {Socket(serverAdr, serverPort) } catch (e: ErrnoException) { null }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getSocket() : Socket?
    {
        if (socket == null)
            println("ZARABOTALO")
        return socket
    }
}