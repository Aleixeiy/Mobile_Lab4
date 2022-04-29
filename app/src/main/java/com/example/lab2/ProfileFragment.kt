package com.example.lab2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.URL

class ProfileFragment : Fragment() {
    private var pref: SharedPreferences? = null

    lateinit var nameField: EditText
    lateinit var surnameField: EditText
    lateinit var emailField: EditText
    lateinit var image_view: ImageView
    lateinit var btn_exit: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        nameField = view.findViewById(R.id.name)
        surnameField = view.findViewById(R.id.surname)
        emailField = view.findViewById(R.id.email)
        btn_exit = view.findViewById(R.id.btn_exit)

        btn_exit.setOnClickListener{
            setStatus(0)
            val editor = pref?.edit()
            editor?.remove("email")
            editor?.apply()
            val enterIntent = Intent(getActivity(), EnterActivity::class.java)
            startActivity(enterIntent)
        }

        pref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
        val prefOther = context?.getSharedPreferences("display_profile", Context.MODE_PRIVATE)
        val isOther = prefOther?.getBoolean("isOther", false)
        var email: String?
        if (isOther == true)
        {
            val btm_menu = view.rootView.findViewById<BottomNavigationView>(R.id.btm_menu)
            btm_menu.isVisible = false
            btn_exit.isVisible = false
            nameField.isEnabled = false
            surnameField.isEnabled = false
            email = prefOther?.getString("email", "")
            val editor = prefOther?.edit()
            editor?.putBoolean("isOther", false)
            editor?.apply()
        }
        else
        email = pref?.getString("email", "")

        image_view = view.findViewById(R.id.image_view)
        if (isOther != true)
        image_view.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED)
                {
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else
                {
                    pickImageFromGallery()
                }
            }
            else
            {
                pickImageFromGallery()
            }
        }

        emailField.setText(email)

        val text = get("http://a0663186.xsph.ru?method=get_user&email=".plus(email))
        if (text == null) {
            val mes = "Нет доступа в интернет"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, mes, duration)
            toast.show()
        }
        val user = Gson().fromJson(text, User::class.java)
        if (user.code == "ok") {
            nameField.setText(user.name)
            surnameField.setText(user.surname)
            if (user.pic != "")
            {
                try {
                    var p = user.pic.replace("*", "\n")
                    p = p.replace("(", "/")
                    p = p.replace(")", "+")
                    val bytes = Base64.decode(p, Base64.DEFAULT)
                    val pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    image_view.setImageBitmap(pic)
                }
                catch (e: Exception)
                {

                }
            }
        }

        nameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val text = get(
                    "http://a0663186.xsph.ru?method=set_name&email=".plus(email).plus("&name=")
                        .plus(nameField.text)
                )
                if (text == null) {
                    val mes = "Нет доступа в интернет"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(context, mes, duration)
                    toast.show()
                }
                val response = Gson().fromJson(text, Response::class.java)
                if (response.code == "ok") {

                }
            }
        })


        surnameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val text = get(
                    "http://a0663186.xsph.ru?method=set_surname&email=".plus(email).plus("&surname=")
                        .plus(surnameField.text)
                )
                if (text == null) {
                    val mes = "Нет доступа в интернет"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(context, mes, duration)
                    toast.show()
                }
                val response = Gson().fromJson(text, Response::class.java)
                if (response.code == "ok") {

                }
            }
        })
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

    private fun pickImageFromGallery()
    {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PIC_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        when(requestCode)
        {
            PERMISSION_CODE ->
            {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    pickImageFromGallery()
                }
                else
                {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PIC_CODE)
        {
            image_view.setImageURI(data?.data)
            val email = pref?.getString("email", "")
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.getContentResolver(), data?.data)
            var pic = encodeImage(bitmap)
            pic = pic?.replace("\n", "*")
            pic = pic?.replace("/", "(")
            pic = pic?.replace("+", ")")


            val text = get(
                "http://a0663186.xsph.ru?method=set_pic&email=".plus(email).plus("&pic=")
                    .plus(pic)
            )
            if (text == null) {
                val mes = "Нет доступа в интернет"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, mes, duration)
                toast.show()
            }
            val response = Gson().fromJson(text, Response::class.java)
            if (response.code == "ok") {

            }
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    fun setStatus(online: Int)
    {
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

    companion object {
        private val IMAGE_PIC_CODE = 1000
        private val PERMISSION_CODE = 1001
    }
}