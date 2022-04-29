package com.example.lab2

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.net.URL


class ChatActivity : AppCompatActivity() {
    private var pref: SharedPreferences? = null
    private var timer: CountDownTimer? = null

    var isCheckNow = false
    lateinit var btn_send: ImageView
    lateinit var txt_message: EditText
    lateinit var lnr_mes: LinearLayout
    lateinit var scr_mes: ScrollView
    lateinit var txt_surname_name: TextView
    lateinit var img_avatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        pref = getSharedPreferences("user", Context.MODE_PRIVATE)
        val email = pref?.getString("email", "")
        val prefTo = getSharedPreferences("message_to", Context.MODE_PRIVATE)
        val emailTo = prefTo?.getString("email_to", "")

        get("http://a0663186.xsph.ru?method=set_received&email1=".plus(email).plus("&email2=").plus(emailTo))

        img_avatar = findViewById(R.id.img_avatar)
        txt_surname_name = findViewById(R.id.txt_surname_name)

        val text = get("http://a0663186.xsph.ru?method=get_user&email=".plus(emailTo))
        val user = Gson().fromJson(text, User::class.java)
        if (user.code == "ok") {
            var title = user.name.plus(" ").plus(user.surname)
            txt_surname_name.setText(title)

            if (user.pic != "")
            {
                try {
                    var p = user.pic.replace("*", "\n")
                    p = p.replace("(", "/")
                    p = p.replace(")", "+")
                    val bytes = Base64.decode(p, Base64.DEFAULT)
                    val pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    img_avatar.setImageBitmap(pic)
                }
                catch (e: Exception)
                {

                }
            }
        }



        txt_message = findViewById(R.id.txt_message)
        btn_send = findViewById(R.id.btn_send)
        lnr_mes = findViewById(R.id.lnr_dial)
        scr_mes = findViewById(R.id.scr_dial)

        this.let {
            KeyboardVisibilityEvent.setEventListener(it,object: KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen)
                    {
                        val parms = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 900)
                        scr_mes.setLayoutParams(parms)
                        scr_mes.setPadding(0, 146, 0, 0)
                    } else
                    {
                        val parms = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1950)
                        scr_mes.setLayoutParams(parms)
                        scr_mes.setPadding(0, 146, 0, 0)
                    }
                }
            })
        }

        btn_send.setOnClickListener{
            val prefTo = getSharedPreferences("message_to", Context.MODE_PRIVATE)
            var mes = txt_message.text.toString()
            mes = mes.replace(" ", "_")
            val email_to = prefTo?.getString("email_to", "")
            val email_from = pref?.getString("email", "")
            val text = get(
                "http://a0663186.xsph.ru?method=send_message&from=".plus(email_from).plus("&to=")
                    .plus(email_to).plus("&text=").plus(mes)
            )
            if (text == null) {
                val mes = "Нет доступа в интернет"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this, mes, duration)
                toast.show()
            }
            val response = Gson().fromJson(text, Response::class.java)
            txt_message.setText("")
        }


        val infinity = 360000000
        timer = object : CountDownTimer(infinity.toLong(), 1000) {
            var count = 0
            override fun onTick(millisUntilFinished: Long) {
                if (!isCheckNow) {
                    isCheckNow = true
                    val prefTo = getSharedPreferences("message_to", Context.MODE_PRIVATE)
                    val email2 = prefTo?.getString("email_to", "")
                    val text = get(
                        "http://a0663186.xsph.ru?method=get_messages&email1=".plus(email)
                            .plus("&email2=").plus(email2)
                    )
                    try {
                        val array = object : TypeToken<ArrayList<Message>>() {}.type
                        val mess: ArrayList<Message> = Gson().fromJson(text, array)
                        addRltLayouts(mess)
                        if (mess.size > count)
                        {
                            count = mess.size
                            scr_mes.fullScroll(View.FOCUS_DOWN)
                            get("http://a0663186.xsph.ru?method=set_received&email1=".plus(email).plus("&email2=").plus(emailTo))
                        }
                    } catch (e: Exception) {

                    }
                    isCheckNow = false
                }
            }

            override fun onFinish() {

            }
        }
        timer?.start();
    }

    fun get(url: String) : String?
    {
        var result: String? = ""
        val url = URL(url)
        GlobalScope.launch {
            try
            {
                result = url.readText()
            }
            catch(e: Exception)
            {
                result = null
            }
        }
        while (result == "")
            Thread.sleep(10)
        return result
    }

    fun addRltLayouts(mess: ArrayList<Message>)
    {
        lnr_mes.removeAllViews()

        for (i in 0..mess.size - 1)
        {
            var last = if (i == mess.size - 1) true else false
            var first = if (i == 0) true else false
            var rlt_mes = getRlt(mess[i], last)
            if (rlt_mes != null)
                lnr_mes.addView(rlt_mes)
        }

        val empty = TextView(this)
        val params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(700, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, 80)
        empty.layoutParams = params
        lnr_mes.addView(empty)
    }

    fun getRlt(mes: Message, last: Boolean): RelativeLayout?
    {
        try {
            val email = pref?.getString("email", "")

            var rlt = RelativeLayout(this)
            rlt.layoutParams =
                RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            var txt_mes = TextView(this)
            mes.text = mes.text.replace("_", " ")
            txt_mes.setText(mes.text)
            txt_mes.setTextSize(16F)
            txt_mes.setPadding(10, 50, 10, 10)
            txt_mes.setTextColor(Color.BLACK)
            txt_mes.setBackgroundColor(Color.rgb(140, 255, 140))
            if (mes.received == 0)
                txt_mes.setBackgroundColor(Color.rgb(100, 215, 100))

            var txt_time = TextView(this)
            txt_time.setText(mes.time)
            txt_time.setTextSize(10F)
            txt_time.setPadding(10, 10, 10, 10)
            txt_time.setTextColor(Color.BLACK)

            var top = 10
            var bottom = 10
            if (last)
                bottom = 100
            val right = 10
            var left = 10
            if (mes.source == email)
            {
                left = 365
                txt_mes.setBackgroundColor(Color.rgb(150, 255, 255))
                if (mes.received == 0)
                    txt_mes.setBackgroundColor(Color.rgb(110, 215, 215))
            }
            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(700, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(left, top, right, bottom)
            txt_mes.setLayoutParams(params)
            txt_time.setLayoutParams(params)

            rlt.addView(txt_mes)
            rlt.addView(txt_time)
            return rlt
        }
        catch(e: Exception)
        {
            return null
        }
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}