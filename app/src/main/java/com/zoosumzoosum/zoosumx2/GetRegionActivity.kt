package com.zoosumzoosum.zoosumx2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zoosumzoosum.zoosumx2.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_get_region.*
import kotlinx.android.synthetic.main.activity_user_name.*

class GetRegionActivity : AppCompatActivity() {

    var fbAuth: FirebaseAuth? = null
    var fbFirestore: FirebaseFirestore? = null

    // 백 버튼 금지
    override fun onBackPressed() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_region)

        fbAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()

        val userInfo = UserDTO()
        val userName = findViewById<TextView>(R.id.textview_username_get_region)
        fbFirestore?.collection("users")?.document(fbAuth?.uid.toString())
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                userName.text = documentSnapshot.data?.get("nickname").toString()
            }

        fbFirestore?.collection("users")?.document(fbAuth?.uid.toString())
            ?.addSnapshotListener{ documentSnapshot, firesbaseFirestoreException ->
                if(documentSnapshot == null) return@addSnapshotListener
                if(documentSnapshot.data?.get("addressRegion")!=null){
                    region_edit.text = documentSnapshot.data?.get("addressRegion").toString()
                }
            }

        val nextButton = findViewById<Button>(R.id.region_button_next)
        nextButton.setOnClickListener {
            if(region_edit.text == "행정구 입력"){
                Toast.makeText(applicationContext, "행정구를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this, IslandNameActivity::class.java)
                startActivity(intent)
            }
        }

        val backButton = findViewById<ImageButton>(R.id.region_button_back)
        backButton.setOnClickListener {
            val intent = Intent(this, UserNameActivity::class.java)
            startActivity(intent)
        }

        if(intent.hasExtra("user_region")) {
            region_edit.text = intent.getStringExtra("user_region")
            userInfo.addressRegion = region_edit.text.toString()

            // firestore에 사용자 정보 업데이트
            fbFirestore?.collection("users")?.document(fbAuth?.uid.toString())
                ?.update("addressRegion", userInfo.addressRegion.toString())

            // 해당구 주민 수 1 증가
            fbFirestore?.collection("region")
                ?.document(intent.getStringExtra("user_region").toString())
                ?.update("people", FieldValue.increment(1))

            fbFirestore?.collection("region")
                ?.document(intent.getStringExtra("user_region").toString())
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot == null) return@addSnapshotListener
                    fbFirestore?.collection("users")?.document(fbAuth?.uid.toString())
                        ?.update("rank", documentSnapshot.data?.get("people").toString().toInt())
                }
        }

        //bottom sheet dialog
        region_edit.setOnClickListener{
            val bottomFragment = BottomFragment()
            bottomFragment.show(supportFragmentManager,"TAG")
        }
    }
}