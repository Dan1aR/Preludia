package com.example.dan1ar.preludia

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.dan1ar.preludia.sendGetMessage.SendMessage
import kotlinx.android.synthetic.main.activity_chat.*
import sendGetMessage.encrypting.Hashes
import java.io.File
import java.io.InputStream
import java.net.Socket
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*


class SendMessageThread(private val sendMessage: SendMessage, private val socket: Socket, private val data : String, private val command : String, private val publicKey: PublicKey?, private val userName : String?, private val userToWhom : String?, private val expansion : String) : Thread()
{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun run()
    {
        when (command)
        {
            "writeCommandToServer" -> sendMessage.writeCommandToServer(data, socket)
            "write" -> sendMessage.write(userName!!, userToWhom!!, data, socket, publicKey!!)
            "writeFile"-> sendMessage.writeFile(userName!!, userToWhom!!, data, socket, publicKey!!, expansion)
        }
    }
}


class ChatActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var userToWhom: String
    private lateinit var publicKey: PublicKey

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        overridePendingTransition(0, 0)

        val fileUserData = File(filesDir, "userData")
        this.userName = fileUserData.readText()

        println("VALERCHIK, ZAVODI")

        val hash = Hashes()
        val allContent = intent.getStringExtra("content")
        val allContentList = allContent.split("|P|")
        val userFromWhom = allContentList[0]
        val fileCurrentUserToWhom = File(filesDir, "currentDialog.dialog")
        this.userToWhom = hash.hash(userFromWhom)
        fileCurrentUserToWhom.writeText(this.userToWhom)
        val mainLayout = findViewById<LinearLayout>(R.id.ChatLinearal)
        mainLayout.removeAllViews()

        // Создаеём публичный ключ
        val context = applicationContext
        var file = File(context.filesDir, "public$userToWhom.key")
        val publicKeyString = file.readText()
        println(publicKeyString)

        file = File(context.filesDir, "$userToWhom.dialog")
        if (!file.exists())
            file.createNewFile()
        else
            for (line in file.readLines())
            {
                println(line.substringAfterLast(" :: "))
                if (line.substringAfterLast(" :: ") == ".txt")
                {
                    val text = line.substringBeforeLast(" :: ")
                    val messageCloud = TextView(this)
                    messageCloud.text = text
                    messageCloud.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
                    if (line.substringBefore(" :: ") == userName)
                    {
                        messageCloud.setTextColor(Color.parseColor("#000000"))
                        messageCloud.setBackgroundResource(R.drawable.chatcloud_bg)
                    }
                    else
                    {
                        messageCloud.setTextColor(Color.parseColor("#FFFFFF"))
                        messageCloud.setBackgroundResource(R.drawable.chatcloud_r_bg)
                    }
                    mainLayout.addView(messageCloud, -1)
                }
                else if (line.substringAfterLast(" :: ") == ".jpg")
                {
                    var path = line.split(" :: ")[1]
                    println("IMG :: $path")
                    if ("/" !in path)
                    {
                        val imgFile = File(context.filesDir, path)
                        if(imgFile.exists())
                        {
                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            val myImage = ImageView(this@ChatActivity)
                            myImage.setImageBitmap(myBitmap)
                            mainLayout.addView(myImage, -1)
                            println("IMG 1")
                        }
                    }
                    else
                    {
                        println("IMG 2")
                        path = path.substringAfter(":")
                        //val uri = Uri.fromFile(File(path))
                        val myImage = ImageView(this@ChatActivity)
                        myImage.setImageURI(Uri.fromFile(File(path)))
                        mainLayout.addView(myImage)

                        //val btm = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))

                    }
                }
            }

        val sv = findViewById<ScrollView>(R.id.scrollView2)
        sv.post { sv.fullScroll(ScrollView.FOCUS_DOWN) }

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpecPublic = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString))
        this.publicKey = keyFactory.generatePublic(keySpecPublic)

    }


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(view: View)
    {
        val internet = Internet()
        val socket = internet.socket
        val context = applicationContext

        val message = messageInput.text.toString()
        if (!message.isBlank())
        {
            messageInput.setText("")

            val mainLayoutLocal = findViewById<LinearLayout>(R.id.ChatLinearal)
            val messageCloud = TextView(this@ChatActivity)
            val sdf = SimpleDateFormat("hh:mm:ss")
            val currentDate = sdf.format(Date())

            messageCloud.text = "$userName :: $message :: $currentDate"
            messageCloud.setTextColor(Color.parseColor("#000000"))
            messageCloud.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
            messageCloud.setBackgroundResource(R.drawable.chatcloud_bg)
            mainLayoutLocal.addView(messageCloud, -1)


            val file = File(context.filesDir, "$userToWhom.dialog")
            file.appendText("$userName :: $message :: $currentDate :: .txt\n")

            val sendMessage = SendMessage()
            val sendMessageThread =
                SendMessageThread(sendMessage, socket!!, message, "write", publicKey, userName, userToWhom, "")
            sendMessageThread.isDaemon = true
            sendMessageThread.start()
        }

    }

    fun sendFile(view: View)
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }
    }


    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK)
        {
            val internet = Internet()
            val socket = internet.socket
            val context = applicationContext

            val selectedFile = data?.data //The uri with the location of the file
            println("path :: ${selectedFile!!.path}")

            val mainLayoutLocal = findViewById<LinearLayout>(R.id.ChatLinearal)
            val messageCloud = ImageView(this@ChatActivity)
            val sdf = SimpleDateFormat("hh:mm:ss")
            val currentDate = sdf.format(Date())
            messageCloud.setImageURI(selectedFile)
            mainLayoutLocal.addView(messageCloud, -1)

            val ins = contentResolver.openInputStream(selectedFile)
            var dataIns = ins!!.read()
            var dataToSend = ""
            while (dataIns != -1)
            {
                dataToSend += "$dataIns "
                dataIns = ins.read()
                println("!")
            }
            println("!!! \n $dataToSend")


            val file = File(context.filesDir, "$userToWhom.dialog")
            val expansion = "." + selectedFile.path.substringAfterLast(".")
            file.appendText("$userName :: ${selectedFile.path} :: $currentDate :: $expansion\n")
            println("!!!!!!!!! expansion :: $expansion")
            val sendMessage = SendMessage()
            val sendMessageThread =
                SendMessageThread(sendMessage, socket!!, dataToSend, "writeFile", publicKey, userName, userToWhom, expansion)
            sendMessageThread.isDaemon = true
            sendMessageThread.start()
        }
    }
}
