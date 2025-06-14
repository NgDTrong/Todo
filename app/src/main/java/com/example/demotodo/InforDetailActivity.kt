package com.example.demotodo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.demotodo.model.Note
import java.io.FileOutputStream

class InforDetailActivity : AppCompatActivity() {

    private lateinit var note: Note
    private lateinit var edtTitle: EditText
    private lateinit var edtContent: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_infor_detail)

        edtTitle = findViewById(R.id.edt_title)
        edtContent = findViewById(R.id.edt_content)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        note = intent.getSerializableExtra("note") as Note
        val position = intent.getIntExtra("position", -1)
        edtTitle.setText(note.tile)
        edtContent.setText(note.content)
        btnBack.setOnClickListener {
            val updateNote = Note("", edtTitle.text.toString(), edtContent.text.toString())
            val resultIntent = Intent()
            resultIntent.putExtra("updateNote", updateNote)
//            resultIntent.putExtra("position",position)
            val listSize = intent.getIntExtra("listSize", 0)
            val randomPos = (0 until listSize).random()
            resultIntent.putExtra("position", randomPos)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }


}
