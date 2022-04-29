package com.example.lab2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.net.URL

class EnterActivity : AppCompatActivity() {
    private var pref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        pref = getSharedPreferences("user", Context.MODE_PRIVATE)
        val email = pref?.getString("email", "")
        if ((email != null) && (email != ""))
        {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
        val name = findViewById<EditText>(R.id.surname)
        name.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus)
            {
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
            else
            {
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    fun onEnterClick(view: View)
    {
        val email = findViewById<EditText>(R.id.surname).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()


        var text = get("http://a0663186.xsph.ru?method=enter&email=".plus(email).plus("&password=").plus(password))
        if (text == null)
        {
            val mes = "Нет доступа в интернет"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, mes, duration)
            toast.show()
            return
        }
        var response = Gson().fromJson(text, Response::class.java)
        if (response.code == "ok")
        {
            val mainIntent = Intent(this, MainActivity::class.java)
            putUser(email)
            startActivity(mainIntent)
        } else
        {
            val mes = response.code
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, mes, duration)
            toast.show()
            return
        }
    }

    fun onRegClick(view: View)
    {
        val email = findViewById<EditText>(R.id.surname).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()


        var text = get("http://a0663186.xsph.ru?method=add_user&email=".plus(email).plus("&password=").plus(password))
        if (text == null)
        {
            val mes = "Нет доступа в интернет"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, mes, duration)
            toast.show()
            return
        }
        var response = Gson().fromJson(text, Response::class.java)
        if (response.code == "ok")
        {
            val mainIntent = Intent(this, MainActivity::class.java)
            putUser(email)
            startActivity(mainIntent)
        } else
        {
            val mes = response.code
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, mes, duration)
            toast.show()
            return
        }
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
            sleep(10)
        return result
    }

    fun putUser(email: String)
    {
        val editor = pref?.edit()
        editor?.putString("email", email)
        editor?.apply()
    }
}