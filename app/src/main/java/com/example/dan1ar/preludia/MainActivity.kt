package com.example.dan1ar.preludia

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.example.dan1ar.preludia.connecting.Connecting
import com.example.dan1ar.preludia.sendGetMessage.GetMessage
import com.example.dan1ar.preludia.sendGetMessage.SendMessage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import sendGetMessage.encrypting.Hashes
import java.io.File
import java.net.Socket


const val GET_USERTOWHOM = 1

class GetMessageTask(private val getMessage: GetMessage, private val socket: Socket, private val context: Context) : AsyncTask<Void, Void, String>() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doInBackground(vararg params : Void?) : String?
    {
        Looper.prepare()
        getMessage.listener(socket, context)
        return null
    }

}
private val connecting = Connecting()
class Internet
{
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val socket = connecting.getSocket()
}

class GetSocketThread(context : Context) : Thread()
{
    private val ctx = context

    override fun run() {
        /*!!!
        ПЕРВЫЙ БЛОК, РАБОТАЮЩИЙ НА ЗАПУСКЕ ЭКРАНА С СПИСКОМ ЧАТОВ
        !!!*/

        Looper.prepare()
        // Экземпляры классов
        val hash = Hashes()
        val sendMessage = SendMessage()
        val internet = Internet()
        val socket = internet.socket
        val getMessage = GetMessage()

        if (socket != null)
        {
            if ("userData" !in ctx.fileList())
            {
                println("NO userData")
                val userName = "reg"
                sendMessage.writeCommandToServer(userName, socket)
                val intentReg = Intent(ctx, RegActivity::class.java)
                intentReg.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.startActivity(intentReg)
            }
            else
            {
                // Отправка первичных данных серверу
                val file = File(ctx.filesDir, "userData")
                val userName = file.readText()
                sendMessage.writeCommandToServer(hash.hash(userName), socket)
            }

            // Запуск слушающего потока
            val getMessageTask = GetMessageTask(getMessage, socket, ctx)
            getMessageTask.execute()
        }
    }
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)

        val context = applicationContext
        var file = File(context.filesDir, "currentDialog.dialog")
        file.writeText("")

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { getUserToWhom() }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


        val getSocketThread = GetSocketThread(context)
        getSocketThread.isDaemon = true
        getSocketThread.start()

        file = File(context.filesDir, "userData")
        if (!file.exists())
        {
            val regInternet = Intent(context, RegActivity::class.java)
            regInternet.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(regInternet)
        }

        createChatListButtons()
    }

    private fun createChatListButtons()
    {
        val myDB = openOrCreateDatabase("ChatList.db", AppCompatActivity.MODE_PRIVATE, null)
        myDB.execSQL("CREATE TABLE IF NOT EXISTS chatList (NAME TEXT)")
        val myCursor = myDB.rawQuery("select name from chatList", null)
        val mainLayout = findViewById<LinearLayout>(R.id.ChatList)
        mainLayout.removeAllViews()

        // Чистка
        fun cleaning()
        {
            myDB.delete("chatList", null, null)
            println("----------------------------------------------------")
            val context = applicationContext
            for (fl in context.fileList()) {
                println("From MAinActivity ::: $fl")
                val file = File(context.filesDir, fl)
                file.delete()
            }
            println("----------------------------------------------------")
            for (fl in context.fileList()) {
                println("From MAinActivity !!! ::: $fl")
            }
        }
        //cleaning()

        // Добавляем новый Button
        var i = 0
        val buttons = mutableListOf<Button>()
        while (myCursor.moveToNext())
        {
            buttons.add(Button(this))
            buttons[i].text = myCursor.getString(0)
            buttons[i].id = i
            buttons[i].setBackgroundResource(R.drawable.my_button_bg)
            buttons[i].setOnClickListener { goToChat(this.findViewById<Button>(it.id).text.toString()) }
            mainLayout.addView(buttons[i], 0)
            i++
        }
        myCursor.close()

    }

    private fun goToChat(userToWhom : String)
    {
        val intentChat = Intent(this, ChatActivity::class.java)
        intentChat.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intentChat.putExtra("content", "$userToWhom|P|null")
        startActivity(intentChat)
    }

    private fun getUserToWhom()
    {
        val intentNewMessageActivity = Intent(applicationContext, NewMessageActivity::class.java)
        //intentNewMessageActivity.type = "txt"
        startActivityForResult(intentNewMessageActivity, GET_USERTOWHOM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GET_USERTOWHOM && resultCode == RESULT_OK)
        {
            createChatListButtons()
        }
        else
        {
            val toast = Toast.makeText(
                applicationContext,
                "Such User ne suschestvuet!",
                Toast.LENGTH_LONG)
            toast.show()
        }
    }


    override fun onBackPressed()
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
        {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId)
        {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        // Handle navigation view item clicks here.
        when (item.itemId)
        {
            R.id.nav_camera ->
            {
                // Handle the camera action
            }
            R.id.nav_gallery ->
            {

            }
            R.id.nav_slideshow ->
            {

            }
            R.id.nav_manage ->
            {

            }
            R.id.nav_share ->
            {

            }
            R.id.nav_send ->
            {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}

