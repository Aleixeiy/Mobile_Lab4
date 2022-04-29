package com.example.lab2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL


class ChatFragment : Fragment() {
    private var pref: SharedPreferences? = null

    lateinit var lnr_dial: LinearLayout

    override fun onStart() {
        super.onStart()

        pref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        lnr_dial = requireView().findViewById(R.id.lnr_dial)

        val pref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val email = pref?.getString("email", "")

        val text = get("http://a0663186.xsph.ru?method=get_dials&email=".plus(email))
        if (text == null)
        {
            val mes = "Нет доступа в интернет"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, mes, duration)
            toast.show()
        } else
        {
            try
            {
                val array = object : TypeToken<ArrayList<Dialog>>() {}.type
                val dials: ArrayList<Dialog> = Gson().fromJson(text, array)
                addRltLayouts(dials)
            }
            catch (e: Exception)
            {

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
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

    fun addRltLayouts(dials: ArrayList<Dialog>)
    {
        lnr_dial.removeAllViews()

        var j = 0
        for (i in 0..dials.size - 1)
            {
                var rlt_dial = getRlt(dials[i])
                j++
                if (j % 2 == 0)
                    rlt_dial?.setBackgroundColor(Color.rgb(255, 235, 59))
                else
                    rlt_dial?.setBackgroundColor(Color.rgb(255, 245, 79))
                if (rlt_dial != null)
                    lnr_dial.addView(rlt_dial)
            }
    }

    fun getRlt(dial: Dialog): RelativeLayout?
    {
        try {
            val email = pref?.getString("email", "")
            var otherEmail = if (dial.source.equals(email, true)) dial.dist else dial.source

            var rlt = RelativeLayout(context)
            rlt.layoutParams =
                RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 210)

            var img = com.google.android.material.imageview.ShapeableImageView(context)
            var p = dial.pic.replace("*", "\n")
            p = p.replace("(", "/")
            p = p.replace(")", "+")
            val bytes = Base64.decode(p, Base64.DEFAULT)
            val pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            img.setImageBitmap(pic)
            img.layoutParams = ViewGroup.LayoutParams(500, ViewGroup.LayoutParams.MATCH_PARENT)
            img.setPadding(-100, 30, 0, 30)

            var txt_name_surname = TextView(context)
            txt_name_surname.setText(dial.name.plus(" ").plus(dial.surname))
            txt_name_surname.setTextSize(16F)
            txt_name_surname.width = 1200
            txt_name_surname.setPadding(350, 10, 0, 30)
            txt_name_surname.setTextColor(Color.BLACK)

            var parms: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            parms.setMargins(350, 80, 0, 30)

            dial.text = dial.text.replace("_", " ")
            val txt_text = TextView(context)
            txt_text.setText(dial.text)
            txt_text.setTextSize(14F)
            txt_text.layoutParams = parms
            txt_text.width = 650
            txt_text.height = 110
            txt_text.setTextColor(Color.BLACK)
            if (dial.source == email)
                txt_text.setBackgroundColor(Color.rgb(150, 255, 255)) else
                txt_text.setBackgroundColor(Color.rgb(140, 255, 140))


            parms = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            parms.setMargins(1040, 80, 0, 30)
            val txt_count = TextView(context)
            if (dial.count > 0)
            txt_count.setText(dial.count.toString())
            txt_count.setTextSize(24F)
            txt_count.layoutParams = parms
            txt_count.setTextColor(Color.rgb(90, 195, 195))

            val img_online = ImageView(context)
            img_online.setPadding(270, 120, 0, 30)
            if (dial.online == 1)
                img_online.setImageResource(R.drawable.ic_online)

            rlt.addView(img)
            rlt.addView(txt_name_surname)
            rlt.addView(txt_text)
            rlt.addView(txt_count)
            rlt.addView(img_online)
            rlt.setOnClickListener{
                val prefTo = context?.getSharedPreferences("message_to", Context.MODE_PRIVATE)
                val editor = prefTo?.edit()
                editor?.putString("email_to", otherEmail)
                editor?.apply()
                val chatIntent = Intent(context, ChatActivity::class.java)
                startActivity(chatIntent)
            }

            return rlt
        }
        catch(e: Exception)
        {
            return null
        }
    }

}