package com.example.lab2

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.text.Editable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.marginLeft
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.lang.Thread.sleep
import java.net.URL
import java.text.AttributedCharacterIterator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var pref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.btm_menu)
        val navController = findNavController(R.id.frg_main)
        bottomNavigationView.setupWithNavController(navController)
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

    override fun onStop() {
        super.onStop()
        setStatus(0)
    }

    override fun onStart() {
        super.onStart()
        setStatus(1)
    }

    fun setStatus(online: Int)
    {
        pref = getSharedPreferences("user", Context.MODE_PRIVATE)
        val emailNow = pref?.getString("email", "")
        if (emailNow != "") {
            val text = get("http://a0663186.xsph.ru?method=get_user&email=".plus(emailNow))
            val user = Gson().fromJson(text, User::class.java)
            if ((user.code == "ok") && (user.online != online)) {
                val text = get(
                    "http://a0663186.xsph.ru?method=set_online&email=".plus(emailNow).plus("&online=")
                        .plus(online.toString())
                )
                var t = text
            }
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
            Thread.sleep(10)
        return result
    }
}
