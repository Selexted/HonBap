package com.example.honbap

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.honbap.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.HashMap
import kotlinx.coroutines.*

class SignUpActivity : AppCompatActivity() {
    lateinit var binding2: ActivitySignUpBinding
    lateinit var rdb: DatabaseReference
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding2.root)
        auth = FirebaseAuth.getInstance()
        init2()
    }
    fun builddialog(text:String,context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("$text").setTitle("$text")
        val dlg = builder.create()
        dlg.show()
    }
    fun init2(){
        rdb= FirebaseDatabase.getInstance().getReference("information")
        binding2.apply{
            var useremail:String
            var userpassword:String
            var userpasswordcertificate:String
            var userage:Int
            var usersex:String?=null
            var usernickname:String
            var certificateflag:Boolean=false
            var auto:Int=0
            val forauto=rdb.child("auto").addListenerForSingleValueEvent(object:
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    //
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    auto=snapshot.value.toString().toInt()
                }

            })
            emailtextinputedittext.addTextChangedListener {
                if(it.toString().contains("@konkuk.ac.kr")){
                    emailtextinputlayout.error = null
                    emailcertificatebtn.isEnabled=true
                }
                else{
                    emailtextinputlayout.error = "??????????????? ???????????? ????????????."
                }
                certificateflag=false
            }
            emailcertificatebtn.setOnClickListener {
                //????????? ??????

                val checkemail=emailtextinputedittext.text.toString()
//                val userpassword = passwordedittext.text.toString()
                emailindatabase(auto,checkemail)
                createUserId(checkemail,"password")
                certificateflag=true
            }
            sexradiogroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId){
                    R.id.manradiobtn->{
                        usersex="???"
                    }
                    R.id.womanradiobtn->{
                        usersex="???"
                    }
                }
            }
            joinbtn.setOnClickListener {
                if(!certificateflag){
                    Toast.makeText(this@SignUpActivity,"???????????? ???????????? ???????????????.",Toast.LENGTH_SHORT).show()
                    Log.i("join","email")
                }
                else if(passwordedittext.text.isEmpty()){
                    Toast.makeText(this@SignUpActivity,"??????????????? ???????????????",Toast.LENGTH_SHORT).show()
                    Log.i("join","password")
                }
                else if(passwordconfirmedittext.text.isEmpty()){
                    Toast.makeText(this@SignUpActivity,"???????????? ????????? ???????????????",Toast.LENGTH_SHORT).show()
                    Log.i("join","passwordconfirm")
                }
                else if(passwordedittext.text.toString()!=passwordconfirmedittext.text.toString()){
                    Toast.makeText(this@SignUpActivity,"??????????????? ???????????? ????????? ???????????? ????????????",Toast.LENGTH_SHORT).show()
                    Log.i("join","passwordonemoretime")
                }
                else if(ageedittext.text.isEmpty()){
                    Toast.makeText(this@SignUpActivity,"????????? ???????????????",Toast.LENGTH_SHORT).show()
                    Log.i("join","age")
                }
                else if(usersex==null){
                    Toast.makeText(this@SignUpActivity,"????????? ???????????????",Toast.LENGTH_SHORT).show()
                    Log.i("join","sex")
                }
                else if(nickname.text.isEmpty()){
                    Toast.makeText(this@SignUpActivity,"???????????? ???????????????",Toast.LENGTH_SHORT).show()
                    Log.i("join","nickname")
                }
                else {
                    auth.currentUser?.reload()

                    Log.i("join","else")
                    useremail = emailtextinputedittext.text.toString()
                    userpassword = passwordedittext.text.toString()
                    userage = ageedittext.text.toString().toInt()
                    usernickname = nickname.text.toString()
                    //firebase??? ???????????????..
                    if(verifyEmail2()) {
                        val loginrdb = rdb.child("$auto")
                        loginrdb.child("uid").setValue(auto)
                        loginrdb.child("userEmail").setValue(useremail)
                        loginrdb.child("userPassword").setValue(userpassword)
                        loginrdb.child("userage").setValue(userage)
                        loginrdb.child("userSex").setValue(usersex)
                        loginrdb.child("userNickname").setValue(usernickname)
                        val hashmap: HashMap<String, Any> = HashMap<String, Any>()
                        hashmap.put("auto", "${auto + 1}")
                        val addauto = rdb.updateChildren(hashmap)
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity,"????????? ????????? ????????????",Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    fun createUserId(id:String, pw:String) {
        Log.v("1", "1")
        auth.createUserWithEmailAndPassword(id,pw).addOnCompleteListener { task->
            if(task.isSuccessful) {
                Log.v("success", "success")
                verifyEmail()
            } else {
                Log.v("asd","Asd")
                Log.w("createUserWithEmail:failure", task.exception)
//                Toast.makeText(this@SignUpActivity,"????????????",Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun verifyEmail() {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("emailsend2", "Email sent.2")
                Toast.makeText(this@SignUpActivity,"???????????? ?????? ???????????????.",Toast.LENGTH_SHORT).show()
            } else {
                Log.v("emailsend2", "Email sent failed.2")
                Toast.makeText(this@SignUpActivity,"????????? ????????? ??????????????????.",Toast.LENGTH_SHORT).show()
                auth.currentUser!!.delete()
            }
        }
    }
    fun verifyEmail2():Boolean {
        runBlocking {
            val job = GlobalScope.launch {
                auth.currentUser?.reload()
            }
            job.join()
        }
        Toast.makeText(this@SignUpActivity,"????????? ?????? ??????????????????.",Toast.LENGTH_SHORT).show()
        Thread.sleep(2000L)
        return auth.currentUser!!.isEmailVerified
    }
    fun emailindatabase(count:Int,userid:String){
        var flag=false
        rdb.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                //
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for(i in 0..(count-1)){
                    val tempemail=snapshot.child("$i").child("userEmail").value.toString()
//                    Log.i("email",tempemail)
                    if(userid==tempemail){
                        flag=true
                        break
                    }

                }
                if(flag){
                    AlertDig("?????? ???????????? ?????? ???????????? ????????????.")
                }else{
                    AlertDig("?????? ???????????? ?????????????????????.")
                }
            }
        })
    }
    fun AlertDig(str:String){
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setMessage(str)
            .setPositiveButton("OK"){
                    _, _->
            }
        val dlg = builder.create()
        dlg.show()
    }
}

