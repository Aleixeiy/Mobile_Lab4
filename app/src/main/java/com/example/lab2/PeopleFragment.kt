package com.example.lab2

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL


class PeopleFragment : Fragment() {
    lateinit var lnr_people: LinearLayout
    lateinit var edt_find: EditText

    var users: ArrayList<User>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try
        {
            val btm_menu = view.rootView.findViewById<BottomNavigationView>(R.id.btm_menu)
            btm_menu.isVisible = true
        }
        catch (e: Exception)
        {

        }

        lnr_people = view.findViewById(R.id.lnr_dial)
        edt_find = view.findViewById(R.id.edt_find)

        edt_find.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                addRltLayouts(users!!, edt_find.text.toString())
            }
        })

        val text = get("http://a0663186.xsph.ru?method=get_all_users")
        if (text == null)
        {
            val mes = "Нет доступа в интернет"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, mes, duration)
            toast.show()
        } else
        {
            try {
                val array = object : TypeToken<ArrayList<User>>() {}.type
                users = Gson().fromJson(text, array)
                addRltLayouts(users!!, "")
            }
            catch(e: Exception)
            {

            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_people, container, false)
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

    fun addRltLayouts(users: ArrayList<User>, text: String)
    {
        lnr_people.removeAllViews()

        var j = 0
        for (i in 0..users.size - 1)
        if ((users[i].name.contains(text, true)) ||
            (users[i].surname.contains(text, true)) ||
            (users[i].name.plus(" ").plus(users[i].surname).contains(text, true)) ||
            (users[i].surname.plus(" ").plus(users[i].name).contains(text, true)))
            {
                var rlt_user = getRlt(users[i])
                j++
                if (j % 2 == 0)
                    rlt_user?.setBackgroundColor(Color.rgb(255, 235, 59))
                else
                    rlt_user?.setBackgroundColor(Color.rgb(255, 245, 79))
                if (rlt_user != null)
                    lnr_people.addView(rlt_user)
            }
    }

    fun getRlt(user: User): RelativeLayout?
    {
        try {

            var rlt = RelativeLayout(context)
            rlt.layoutParams =
                RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 210)

            var img = com.google.android.material.imageview.ShapeableImageView(context)
            var p = user.pic.replace("*", "\n")
            p = p.replace("(", "/")
            p = p.replace(")", "+")
            val bytes = Base64.decode(p, Base64.DEFAULT)
            val pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            img.setImageBitmap(pic)
            img.layoutParams = ViewGroup.LayoutParams(500, ViewGroup.LayoutParams.MATCH_PARENT)
            img.setPadding(-100, 30, 0, 30)
            img.setOnClickListener{
                val prefOther = context?.getSharedPreferences("display_profile", Context.MODE_PRIVATE)
                val editor = prefOther?.edit()
                editor?.putBoolean("isOther", true)
                editor?.apply()
                editor?.putString("email", user.email)
                editor?.apply()
                val navController = activity?.findNavController(R.id.frg_main)
                navController?.navigate(R.id.profileFragment)
            }

            var txt_name = TextView(context)
            txt_name.setText(user.name)
            txt_name.setTextSize(24F)
            txt_name.width = 900
            txt_name.setPadding(350, 10, 0, 30)
            txt_name.setTextColor(Color.BLACK)

            var txt_surname = TextView(context)
            txt_surname.setText(user.surname)
            txt_surname.setTextSize(24F)
            txt_surname.width = 900
            txt_surname.setPadding(350, 85, 0, 30)
            txt_surname.setTextColor(Color.BLACK)

            val send = TextView(context)
            send.setText("написать")
            send.setTextSize(20F)
            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(850, 70, 10, 10)
            send.setLayoutParams(params)

            send.setTextColor(Color.BLACK)
            send.setBackgroundColor(Color.rgb(140, 255, 140))
            send.setOnClickListener{
                val prefTo = context?.getSharedPreferences("message_to", Context.MODE_PRIVATE)
                val editor = prefTo?.edit()
                editor?.putString("email_to", user.email)
                editor?.apply()
                val chatIntent = Intent(context, ChatActivity::class.java)
                startActivity(chatIntent)
            }

            val img_online = ImageView(context)
            img_online.setPadding(270, 120, 0, 30)
            if (user.online == 1)
            img_online.setImageResource(R.drawable.ic_online)

            rlt.addView(img)
            rlt.addView(txt_name)
            rlt.addView(txt_surname)
            rlt.addView(send)
            rlt.addView(img_online)

            return rlt
        }
        catch(e: Exception)
        {
            return null
        }
    }

}