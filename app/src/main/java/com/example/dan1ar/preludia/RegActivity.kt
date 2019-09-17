package com.example.dan1ar.preludia

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.dan1ar.preludia.sendGetMessage.SendMessage
import kotlinx.android.synthetic.main.activity_reg.*
import sendGetMessage.encrypting.Hashes
import java.io.File


class RegActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickReg(view : View)
    {
        val userName = userNameInput.text.toString()
        println("!!!!!!!!!!!!! $userName")
        if (userName.isNotBlank() && userName.isNotEmpty())
        {
            val context = applicationContext

            val sendMessage = SendMessage()
            val hash = Hashes()
            val internetReg = Internet()
            val socket = internetReg.socket

            val sendMessageThread = SendMessageThread(
                sendMessage,
                socket!!,
                hash.hash(userName),
                "writeCommandToServer",
                null,
                null,
                null,
                ""
            )
            sendMessageThread.isDaemon = true
            sendMessageThread.start()

            println("SENDED :: ${hash.hash(userName)}")
            Thread.sleep(1500)

            val files = context.fileList()
            if ("userData" in files)
            {
                var file = File(context.filesDir, "userData")
                file.writeText(userName)

                file = File(context.filesDir, "PublicKey.pem")
                val publicKey = file.readText()

                val sendMessageThreadP = SendMessageThread(
                    sendMessage,
                    socket,
                    publicKey,
                    "writeCommandToServer",
                    null,
                    null,
                    null,
                    ""
                )
                sendMessageThreadP.isDaemon = true
                sendMessageThreadP.start()

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            else
            {
                val toast = Toast.makeText(context,
                    "Such userName uze zanyat",
                    Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }
}


