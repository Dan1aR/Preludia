package com.example.dan1ar.preludia

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.dan1ar.preludia.sendGetMessage.SendMessage
import kotlinx.android.synthetic.main.activity_new_message.*
import sendGetMessage.encrypting.Hashes
import java.io.File

class NewMessageActivity : AppCompatActivity()
{
    companion object
    {
        const val FindUserToWhom = "userToWhomFind"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

    }

    fun onButtonClick(view : View)
    {
        val hash = Hashes()
        val userToWhom = userToWhomInput.text.toString()
        if (userToWhom.isNotBlank() && userToWhom.isNotEmpty())
        {
            val context = applicationContext
            val files0 = context.fileList()

            val sendMessage = SendMessage()
            val internet = Internet()
            val socket = internet.socket

            println("$userToWhom :: ${hash.hash(userToWhom)}")
            val sendMessageThread = SendMessageThread(
                sendMessage,
                socket!!,
                "get_public_key\$p${hash.hash(userToWhom)}",
                "writeCommandToServer",
                null,
                null,
                null,
                ""
            )
            sendMessageThread.isDaemon = true
            sendMessageThread.start()
            Thread.sleep(500)

            val files1 = context.fileList()

            for (fl in files0)
                println("From files0 ::: $fl")

            for (fl in files1)
                println("From files1 ::: $fl")

            val myDB = openOrCreateDatabase("ChatList.db", MODE_PRIVATE, null)
            myDB.execSQL("CREATE TABLE IF NOT EXISTS chatList (NAME TEXT)")
            val intentUserToWhom = Intent()

            if (files0.isNotEmpty() && files1.isNotEmpty())
            {
                if (files0.size < files1.size)
                {
                    intentUserToWhom.putExtra(FindUserToWhom, userToWhom)
                    val contentUserToWhom = ContentValues()
                    contentUserToWhom.put("NAME", userToWhom)
                    myDB.insert("chatList", null, contentUserToWhom)
                    setResult(RESULT_OK, intentUserToWhom)
                    finish()
                }
                else
                {
                    setResult(RESULT_CANCELED, intentUserToWhom)
                    finish()
                }
            }
            else
                if (files0.isEmpty() && files1.isNotEmpty())
                {
                    val file = File(context.filesDir, "/${hash.hash(userToWhom)}")
                    file.mkdir()

                    intentUserToWhom.putExtra(FindUserToWhom, userToWhom)
                    val contentUserToWhom = ContentValues()
                    contentUserToWhom.put("NAME", userToWhom)
                    myDB.insert("chatList", null, contentUserToWhom)
                    setResult(RESULT_OK, intentUserToWhom)
                    finish()
                }
        }
    }

}
