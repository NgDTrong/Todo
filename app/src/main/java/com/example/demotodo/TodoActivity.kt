package com.example.demotodo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demotodo.DAO.TodoDAO
import com.example.demotodo.adapter.TodoAdapter
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.fragment.UpdateTodoFragment
import com.example.demotodo.model.Session
import com.example.demotodo.model.Todo
import com.example.demotodo.service.NotificationActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.reflect.Method
import java.util.jar.Manifest

class TodoActivity : AppCompatActivity(), TodoAdapter.OnClickItemTodo {

    private lateinit var fullList: ArrayList<Todo>
    private lateinit var list: ArrayList<Todo>
    private lateinit var adapter: TodoAdapter
    private lateinit var todoDAO: TodoDAO
    private var currentPage = 0
    private val itemSPrePage = 5
    private lateinit var textPage: TextView
    private lateinit var edtSearch: EditText
    private var selectedImageBytes = mutableListOf<ByteArray>()
    private var imagePreviews = mutableListOf<ImageView>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

            })
        } else {
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.w("FCM", "Fetching FCM registration token:$token")
            })
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uri ->
            uri?.let {
                val remainingSlots = 3 - selectedImageBytes.size
                val imagesToAdd = it.take(remainingSlots)
                for (uri in imagesToAdd) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    if (bytes != null) {
                        selectedImageBytes.add(bytes)
                    }
                }
                updateImagePreviews()
            }
        }

    private fun updateImagePreviews() {
        imagePreviews.forEachIndexed { index, imageView ->
            if (index < selectedImageBytes.size) {
                val bitmap = BitmapFactory.decodeByteArray(
                    selectedImageBytes[index],
                    0,
                    selectedImageBytes[index].size
                )
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE
            } else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_todo)
        val data: Uri? = intent?.data
        val userIdFromLink = data?.getQueryParameter("userId")?.toIntOrNull()
        val currentUserId = userIdFromLink ?: Session.currentUserId
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("FCM", "#$token")
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
        todoDAO = AppDatabase.getDatabase(this).todoDAO()
        fullList = ArrayList(todoDAO.getAllByUser(currentUserId))
        textPage = findViewById(R.id.text_page)
        edtSearch = findViewById(R.id.edt_search)
        adapter = TodoAdapter(this, arrayListOf(), this)
        edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchTodo()
                true
            } else false
        }
        findViewById<RecyclerView>(R.id.rv_todo).apply {
            layoutManager = LinearLayoutManager(this@TodoActivity)
            adapter = this@TodoActivity.adapter
        }

        findViewById<ImageView>(R.id.btn_add).setOnClickListener { showDialog() }
        findViewById<ImageView>(R.id.img_share).setOnClickListener {
            val link = "todoapp://todo?userId=${currentUserId}"
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Công việc", link)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Liên kết đã được copy", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageView>(R.id.img_bell).setOnClickListener {
            intent=Intent(this,NotificationActivity::class.java)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.btn_pre).setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateList()
            }
        }

        findViewById<ImageView>(R.id.btn_next).setOnClickListener {
            val maxPage = (fullList.size - 1) / itemSPrePage
            if (currentPage < maxPage) {
                currentPage++
                updateList()
            }
        }

        updateList()
    }

    private fun searchTodo() {
        val keyword = edtSearch.text.toString().trim()
        fullList = ArrayList(todoDAO.searchTodo(Session.currentUserId, keyword))
        currentPage = 0
        updateList()
    }

    private fun updateList() {
        list = getNotesForPage(currentPage)
        adapter.updateData(list)
        updatePageIndicator()
    }

    @SuppressLint("SetTextI18n")
    private fun updatePageIndicator() {
        val totalPage = if (fullList.isEmpty()) 1 else (fullList.size - 1) / itemSPrePage + 1
        textPage.text = "Trang ${currentPage + 1} / $totalPage"
    }

    private fun getNotesForPage(page: Int): ArrayList<Todo> {
        val fromIndex = page * itemSPrePage
        val toIndex = minOf(fromIndex + itemSPrePage, fullList.size)
        return if (fromIndex < toIndex) ArrayList(
            fullList.subList(
                fromIndex,
                toIndex
            )
        ) else arrayListOf()
    }

    private fun showDialog() {
        selectedImageBytes.clear()
        val dialogAdd = LayoutInflater.from(this).inflate(R.layout.layout_add_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogAdd)
        val alertDialog = dialogBuilder.show()

        val edtTitle = dialogAdd.findViewById<EditText>(R.id.edt_title)
        val edtContent = dialogAdd.findViewById<EditText>(R.id.edt_content)
        val imgPre = dialogAdd.findViewById<LinearLayout>(R.id.img_preview)
        val btnCancel = dialogAdd.findViewById<AppCompatButton>(R.id.btn_cancel)
        val btnAdd = dialogAdd.findViewById<AppCompatButton>(R.id.btn_add)

        imagePreviews = mutableListOf()
        imgPre.removeAllViews()
        repeat(3) {
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(
                this.resources.getDimensionPixelSize(R.dimen.img_with),
                this.resources.getDimensionPixelSize(R.dimen.img_height),
                1f
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.visibility = View.GONE
            imgPre.addView(imageView)
            imagePreviews.add(imageView)
        }

        imgPre.setOnClickListener {
            if (selectedImageBytes.size >= 3) {
                Toast.makeText(this, "Đã đạt tối đa 3 ảnh", Toast.LENGTH_SHORT).show()
            } else {
                imagePickerLauncher.launch("image/*")
            }
        }

        btnCancel.setOnClickListener { alertDialog.dismiss() }

        btnAdd.setOnClickListener {
            val title = edtTitle.text.toString()
            val content = edtContent.text.toString()

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "Dữ liệu không được trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val todo = Todo(
                title = title,
                content = content,
                userId = Session.currentUserId,
                imageData = selectedImageBytes.toList()
            )
            try {
                todoDAO.insertTodo(todo)
                Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
                fullList = ArrayList(todoDAO.getAllByUser(Session.currentUserId))
                currentPage = 0
                updateList()
                alertDialog.dismiss()
                selectedImageBytes.clear()
                updateImagePreviews()
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi khi thêm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDeleteTodo(todo: Todo) {
        todoDAO.deleteTodo(todo)
        fullList = ArrayList(todoDAO.getAllByUser(Session.currentUserId))
        if (currentPage > 0 && getNotesForPage(currentPage).isEmpty()) {
            currentPage--
        }
        updateList()
        Toast.makeText(this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show()
    }

    override fun onUpdateTodo(todo: Todo) {
        val fragment = UpdateTodoFragment()
        val bundle = Bundle()
        bundle.putSerializable("todo_id", todo.id)
        fragment.arguments = bundle
        findViewById<RecyclerView>(R.id.rv_todo).visibility = View.GONE
        findViewById<ImageView>(R.id.btn_add).visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun refreshList() {
        fullList = ArrayList(todoDAO.getAllByUser(Session.currentUserId))
        currentPage = 0
        updateList()
    }


}