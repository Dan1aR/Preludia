//
package com.example.dan1ar.preludia.sendGetMessage

import android.os.Build
import android.support.annotation.RequiresApi
import com.example.dan1ar.preludia.sendGetMessage.encrypting.AESCrypt
import sendGetMessage.encrypting.Hashes
import com.example.dan1ar.preludia.sendGetMessage.encrypting.RSACrypt
import java.io.PrintWriter
import java.net.Socket
import java.security.PublicKey
import java.util.*

class SendMessage
{
    private val hash = Hashes()
    private val rsaCrypt = RSACrypt()
    private val aesCrypt = AESCrypt()

    fun writeCommandToServer (data: String?, socket: Socket)
    {
        val writer = PrintWriter(socket.getOutputStream())
        writer.write(data!!)
        writer.flush()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun write(userName: String, userToWhom: String, data: String?, socket: Socket, publicKey: PublicKey)
    {
        val secretKey = aesCrypt.generateKey()
        val dataInAES = "$userName\$ptext\$p$data\$p.txt"
        val array = aesCrypt.encrypt(secretKey, dataInAES)

        //println("$secretKey ${Base64.getEncoder().encodeToString(secretKey.encoded)}")

        val dataToSend = userToWhom + "\$p" + hash.hash(data!!) + "\$p" + rsaCrypt.encrypt(Base64.getEncoder().encodeToString(secretKey.encoded), publicKey) + "\$p" + array[0] + "\$p" + rsaCrypt.encrypt(array[1], publicKey) + "!end"
        val writer = PrintWriter(socket.getOutputStream())
        writer.write(dataToSend)
        writer.flush()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun writeFile(userName: String, userToWhom: String, data: String?, socket: Socket, publicKey: PublicKey, expansion : String)
    {
        val secretKey = aesCrypt.generateKey()
        val dataInAES = "$userName\$ptext\$p$data\$p$expansion"
        val array = aesCrypt.encrypt(secretKey, dataInAES)

        //println("$secretKey ${Base64.getEncoder().encodeToString(secretKey.encoded)}")

        val dataToSend = userToWhom + "\$p" + hash.hash(data!!) + "\$p" + rsaCrypt.encrypt(Base64.getEncoder().encodeToString(secretKey.encoded), publicKey) + "\$p" + array[0] + "\$p" + rsaCrypt.encrypt(array[1], publicKey) + "!end"
        val writer = PrintWriter(socket.getOutputStream())
        writer.write(dataToSend)
        writer.flush()
    }
}