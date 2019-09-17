//
package com.example.dan1ar.preludia.sendGetMessage

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.example.dan1ar.preludia.ChatActivity
import com.example.dan1ar.preludia.R
import com.example.dan1ar.preludia.sendGetMessage.encrypting.AESCrypt
import sendGetMessage.encrypting.Hashes
import com.example.dan1ar.preludia.sendGetMessage.encrypting.RSACrypt
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.Socket
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class GetMessage
{
    private val hash = Hashes()
    private val rsaCrypt = RSACrypt()
    private val aesCrypt = AESCrypt()
    private lateinit var privateKey : PrivateKey
    private val size = 1024
    private val buffer = ByteArray(size)
    private var byteArrayOutputStream = ByteArrayOutputStream(size)

    //private val bufferPublicKey = BufferPublicKey()


    @SuppressLint("SimpleDateFormat")
    @TargetApi(Build.VERSION_CODES.O_MR1)
    @RequiresApi(Build.VERSION_CODES.O)
    fun listener(socket : Socket, context : Context)
    {
        val inputStream = socket.getInputStream()
        var bytesRead: Int
        var data = ""
        var publicKey : String

        if ("PrivateKey.pem" in context.fileList())
        {
            val file = File(context.filesDir, "PrivateKey.pem")
            val privateKeyString = file.readText()
            val keyFactory = KeyFactory.getInstance("RSA")!!
            val keySpecPrivate = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
            privateKey = keyFactory.generatePrivate(keySpecPrivate)
        }

        while (true)
        {
            while (true)
            {
                bytesRead = inputStream.read(buffer)

                if (bytesRead == -1)
                    break
                else
                    println("!!!!!!!!!!!!!GOT SOMETHING!!!!!!!!!!!!!!!!")

                byteArrayOutputStream.write(buffer, 0, bytesRead)
                data += byteArrayOutputStream.toString()
                byteArrayOutputStream = ByteArrayOutputStream(size)

                if (data.substringAfterLast("!") == "end")
                {
                    if (data.substringBefore(" :: ") == "public_key_of")
                    {
                        println("GOT THE PUBLIC KEY")
                        val keyData = data.split(" :: ")
                        val userToWhom = keyData[1]
                        publicKey = data.substringAfterLast(" :: ").dropLast(4)
                        data = ""
                        if (publicKey == "netKlucha")
                        {
                            println("Klucha net, delo dryan")
                            break
                        }
                        else
                        {
                            println("Create FILE with Public key")

                            val file = File(context.filesDir, "public$userToWhom.key")
                            file.writeText(publicKey)
                            println("FILE CREATED")

                            break
                        }
                    }
                    else if ((data.dropLast(4) == "ZAEBOK") || (data.dropLast(4) == "NE"))
                    {
                        data = data.dropLast(4)
                        if (data == "ZAEBOK")
                        {
                            println("USER CREATED")
                            var file = File(context.filesDir, "userData")
                            file.createNewFile()
                            val rsaCrypt = RSACrypt()
                            rsaCrypt.generateKeyPair(context)

                            file = File(context.filesDir, "PrivateKey.pem")
                            val privateKeyString = file.readText()
                            val keyFactory = KeyFactory.getInstance("RSA")!!
                            val keySpecPrivate = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
                            privateKey = keyFactory.generatePrivate(keySpecPrivate)

                        }
                        data = ""
                    }
                    else
                    {
                        //println("got :: ${data.dropLast(4)}")
                        data = data.dropLast(4)

                        val array = data.split("\$p")
                        for (i in array)
                            println(i)
                        val secretKey = rsaCrypt.decrypt(array[1], privateKey)
                        val iv = rsaCrypt.decrypt(array[3], privateKey)

                        val allData = aesCrypt.decrypt(secretKey!!, iv!!, array[2]).split("\$p")
                        val userFromWhom = allData[0]
                        val contentType = allData[1]
                        val content = allData[2]
                        val contentExpansion = allData[3]

                        if (hash.hash(content) == array[0])
                        {
                            // Вывод Сообщения
                            if (contentExpansion == ".txt")
                            {
                                println("PRISHEL TEXTOVIY CONTENT ::: $userFromWhom :: $content")

                                var file = File(context.filesDir, "${hash.hash(userFromWhom)}.dialog")
                                val sdf = SimpleDateFormat("hh:mm:ss")
                                val currentDate = sdf.format(Date())
                                file.appendText("$userFromWhom :: $content :: $currentDate :: .txt\n")

                                file = File(context.filesDir, "currentDialog.dialog")
                                if (file.readText() == hash.hash(userFromWhom)) {
                                    val intentShowMessageCloudInChatActivity = Intent(context, ChatActivity::class.java)
                                    intentShowMessageCloudInChatActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    intentShowMessageCloudInChatActivity.putExtra("content", "$userFromWhom|P|$content")
                                    context.startActivity(intentShowMessageCloudInChatActivity)
                                }
                                else
                                {

                                    val builder = NotificationCompat.Builder(context).setContentTitle(userFromWhom).setContentText(content)
                                    val notification = builder.build()
                                    val notificationManager = NotificationManagerCompat.from(context)
                                    notificationManager.notify(1, notification)
                                }

                            }
                            else
                            {
                                println(content)
                                println("PRISHLO! $contentExpansion")

                                var sdf = SimpleDateFormat("yyyy_MM_dd::hh:mm:ss")
                                val currentDateFile = sdf.format(Date())
                                var file = File(context.filesDir, "$currentDateFile${hash.hash(userFromWhom)}$contentExpansion")
                                val bytes : MutableList<Byte> = mutableListOf()
                                for (byte in content.split(" "))
                                {
                                    if (byte.isNotEmpty())
                                        bytes.add(byte.toInt().toByte())
                                }
                                file.writeBytes(bytes.toByteArray())

                                file = File(context.filesDir, "${hash.hash(userFromWhom)}.dialog")
                                sdf = SimpleDateFormat("hh:mm:ss")
                                val currentDate = sdf.format(Date())
                                file.appendText("$userFromWhom :: $currentDateFile${hash.hash(userFromWhom)}$contentExpansion :: $currentDate :: $contentExpansion\n")

                                file = File(context.filesDir, "currentDialog.dialog")
                                if (file.readText() == hash.hash(userFromWhom)) {
                                    val intentShowMessageCloudInChatActivity = Intent(context, ChatActivity::class.java)
                                    intentShowMessageCloudInChatActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    intentShowMessageCloudInChatActivity.putExtra("content", "$userFromWhom|P|$content")
                                    context.startActivity(intentShowMessageCloudInChatActivity)
                                }
                                else
                                {

                                    val builder = NotificationCompat.Builder(context)
                                        .setContentTitle(userFromWhom)
                                        .setContentText("File $contentExpansion")
                                        .setSmallIcon(R.drawable.send)
                                    val notification = builder.build()
                                    val notificationManager = NotificationManagerCompat.from(context)
                                    notificationManager.notify(1, notification)
                                }
                            }
                        }

                        data = ""
                        break
                    }
                }
            }
        }
    }
}