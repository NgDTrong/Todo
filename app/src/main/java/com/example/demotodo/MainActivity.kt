package com.example.demotodo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demotodo.adapter.NoteAdapter
import com.example.demotodo.model.Note
import java.io.FileNotFoundException
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), NoteAdapter.OnNoteDeleteListener {
    private lateinit var list: ArrayList<Note>
    private lateinit var pageNote: ArrayList<Note>
    private lateinit var adapter: NoteAdapter
    private var currentPage = 0
    private val itemsPerPage = 5
    private lateinit var textPage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rvNote = findViewById<RecyclerView>(R.id.rvNote)
        rvNote.layoutManager = LinearLayoutManager(this)
        textPage = findViewById(R.id.text_page)
        //Tải dữ liệu từ file hoặc thêm dữ liệu mẫu nếu file trống:
        list = loadNoteFromFile()
        if (list.isEmpty()) {
            list = arrayListOf(
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note("", "Trọng", "Nguyễn Đức Trọng"),
                Note(
                    "",
                    "Trọng",
                    "Nguyễn Đức Trọng\n02/09/2003 \nTDP Văn Sơn, thị trấn Lập Thạch\n huyện Lập Thạch, tỉnh Vĩnh Phúc"
                )
            )
            saveNoteToFile(list)
        }
        // Lấy dữ liệu cho trang đầu tiên, khởi tạo adapter và gán cho RecyclerView:
        pageNote = getNotesForPage(currentPage)
        adapter = NoteAdapter(this, pageNote, this)
        rvNote.adapter = adapter
        findViewById<ImageView>(R.id.btn_add).setOnClickListener { showDialog() }
        //Thiết lập sự kiện cho các nút next trang quay lại
        findViewById<ImageView>(R.id.btn_pre).setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateList()
            }
        }
        findViewById<ImageView>(R.id.btn_next).setOnClickListener {
            val maxPage = (list.size - 1) / itemsPerPage
            if (currentPage < maxPage) {
                currentPage++
                updateList()
            }
        }

        updatePageIndicator()
    }

    private fun updateList() {

        val pageData = getNotesForPage(currentPage)
        adapter.updateData(pageData)
        updatePageIndicator()
    }

    private fun updatePageIndicator() {
        val totalPage = if (list.isEmpty()) 1 else (list.size - 1) / itemsPerPage + 1
        textPage.text = "Trang ${currentPage + 1} / $totalPage"
    }

    private fun getNotesForPage(page: Int): ArrayList<Note> {
        val fromIndex = page * itemsPerPage
        val toIndex = minOf(fromIndex + itemsPerPage, list.size)
        if (fromIndex >= toIndex) return arrayListOf()
        return ArrayList(list.subList(fromIndex, toIndex))
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showDialog() {
        val dialogAdd = LayoutInflater.from(this).inflate(R.layout.layout_add_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogAdd)
        val alertDialog = dialogBuilder.show()

        val edtTitle = alertDialog.findViewById<EditText>(R.id.edt_title)
        val edtContent = alertDialog.findViewById<EditText>(R.id.edt_content)
        val btnCancel = alertDialog.findViewById<AppCompatButton>(R.id.btn_cancel)
        val btnAdd = alertDialog.findViewById<AppCompatButton>(R.id.btn_add)

        btnCancel.setOnClickListener { alertDialog.dismiss() }
        //Thêm item
        btnAdd.setOnClickListener {
            val title = edtTitle?.text.toString()
            val content = edtContent?.text.toString()
            val randomIndex = (0..list.size).random()
            if (title.isNotBlank() && content.isNotBlank()) {
                val newNote = Note("", title, content, isNew = true)
                val rvNote = findViewById<RecyclerView>(R.id.rvNote)
                list.add(randomIndex, newNote)
                currentPage = randomIndex / itemsPerPage
                saveNoteToFile(list)
                updateList()
                val relativeIndex = randomIndex % itemsPerPage
                newNote.isNew = false
                updateList()
                rvNote.scrollToPosition(relativeIndex)
//                Handler(Looper.getMainLooper()).postDelayed({
//                    newNote.isNew = false
//                    updateList()
//                    rvNote.scrollToPosition(randomIndex)
//                }, 2000)
                saveNoteToFile(list)
                alertDialog.dismiss()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val updatedNote = data.getSerializableExtra("updateNote") as? Note
            val position = data.getIntExtra("position", -1)
            if (updatedNote != null && position in list.indices) {
                list[position] = updatedNote
                adapter.notifyItemChanged(position)
                Handler(Looper.getMainLooper()).postDelayed({
                    updatedNote.isNew = false
                    adapter.notifyItemChanged(position)
                }, 2000)
                updateList()
            }
        }
    }

    override fun onDelete(note: Note) {
        val index = list.indexOf(note)
        if (index != -1) {
            val rvNote = findViewById<RecyclerView>(R.id.rvNote)
            // Kiểm tra nếu note nằm trong trang hiện tại thì animation
            val fromIndex = currentPage * itemsPerPage
            val toIndex = minOf(fromIndex + itemsPerPage, list.size)
            if (index in fromIndex until toIndex) {
                val indexInpage = index - fromIndex
                val viewHolder = rvNote.findViewHolderForAdapterPosition(indexInpage)
                viewHolder?.itemView?.animate()?.alpha(0.1f)?.setDuration(2000)?.withEndAction {
                    list.removeAt(index)
                    saveNoteToFile(list)
                    updateList()
                }?.start()
            } else {
                list.removeAt(index)
                saveNoteToFile(list)
                updateList()
            }

        }
    }

    private fun saveNoteToFile(list: ArrayList<Note>) {
        try {
            val outputStream: FileOutputStream = openFileOutput("trong.txt", Context.MODE_PRIVATE)
            for (note in list) {
                outputStream.write("${note.tile}|||${note.content}\n".toByteArray())
            }
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun loadNoteFromFile(): ArrayList<Note> {
        val result = ArrayList<Note>()
        try {
            val input = openFileInput("trong.txt")
            val lines = input.bufferedReader().readLines()
            for (line in lines) {
                val parts = line.split("|||")
                if (parts.size == 2) {
                    result.add(Note("", parts[0], parts[1]))
                }
            }
            input.close()
        } catch (e: FileNotFoundException) {
        }
        return result
    }
}