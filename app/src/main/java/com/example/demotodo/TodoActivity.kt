package com.example.demotodo

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demotodo.adapter.TodoAdapter
import com.example.demotodo.databinding.ActivityTodoBinding
import com.example.demotodo.databinding.LayoutAddDialogBinding
import com.example.demotodo.model.Session
import com.example.demotodo.model.Todo
import com.example.demotodo.repository.TodoRepository
import com.example.demotodo.viewmodel.TodoViewModel
import com.example.demotodo.viewmodel.TodoViewModelFactory
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.fragment.UpdateTodoFragment
import java.io.InputStream

class TodoActivity : AppCompatActivity(), TodoAdapter.OnClickItemTodo {

    private lateinit var binding: ActivityTodoBinding
    private lateinit var adapter: TodoAdapter
    private var selectedImageBytes = mutableListOf<ByteArray>()

    private val viewModel: TodoViewModel by viewModels {
        val dao = AppDatabase.getDatabase(applicationContext).todoDAO()
        TodoViewModelFactory(TodoRepository(dao), Session.currentUserId)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris?.let {
                val toAdd = it.take(3 - selectedImageBytes.size)
                toAdd.forEach { uri ->
                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    inputStream?.readBytes()?.let { bytes -> selectedImageBytes.add(bytes) }
                }
                updatePreviews()
            }
        }

    private lateinit var imagePreviews: List<android.widget.ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.setFragmentResultListener("todo_updated", this) { _, bundle ->
            if (bundle.getBoolean("updated", false)) {
                viewModel.reloadTodosFromDatabase {
                    refreshList()
                }
            }
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter(this, arrayListOf(), this)
        binding.rvTodo.layoutManager = LinearLayoutManager(this)
        binding.rvTodo.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.todos.observe(this) {
            refreshList()
        }
        viewModel.currentPage.observe(this) {
            refreshList()
        }
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            viewModel.nextPage()
        }

        binding.btnPre.setOnClickListener {
            viewModel.previousPage()
        }

        binding.btnAdd.setOnClickListener {
            showAddTodoDialog()
        }

        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = binding.edtSearch.text.toString().trim()
                viewModel.search(keyword)
                true
            } else false
        }
    }

    fun refreshList() {
        adapter.updateData(viewModel.getNotesForCurrentPage())
        updatePageIndicator()
    }

    private fun updatePageIndicator() {
        val totalPages = viewModel.getTotalPages()
        val currentPage = viewModel.currentPage.value ?: 0
        binding.textPage.text = "Trang ${currentPage + 1} / $totalPages"
    }

    override fun onDeleteTodo(todo: Todo) {
        viewModel.delete(todo)
        Toast.makeText(this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show()
    }

    override fun onUpdateTodo(todo: Todo) {
        val fragment = UpdateTodoFragment()
        val bundle = Bundle()
        bundle.putInt("todo_id", todo.id)
        fragment.arguments = bundle
        binding.rvTodo.visibility = View.GONE
        binding.btnAdd.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
//        Toast.makeText(this, "Chức năng sửa chưa triển khai", Toast.LENGTH_SHORT).show()
    }

    private fun showAddTodoDialog() {
        selectedImageBytes.clear()
        val dialogBinding = LayoutAddDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()


        dialogBinding.imgPreview.removeAllViews()

        // Tạo list imagePreviews và thêm vào layout
        imagePreviews = List(3) { index ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.img_with), // ví dụ 100dp
                    resources.getDimensionPixelSize(R.dimen.img_height) // ví dụ 100dp
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    if (index < selectedImageBytes.size) {
                    } else if (selectedImageBytes.size < 3) {
                        imagePickerLauncher.launch("image/*")
                    }
                }
            }
            dialogBinding.imgPreview.addView(imageView)
            imageView
        }


        updatePreviews()

        dialogBinding.imgPreview.setOnClickListener {
            if (selectedImageBytes.size < 3) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Tối đa 3 ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnAdd.setOnClickListener {
            val title = dialogBinding.edtTitle.text.toString()
            val content = dialogBinding.edtContent.text.toString()
            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val todo = Todo(
                title = title,
                content = content,
                userId = Session.currentUserId,
                imageData = selectedImageBytes.toList()
            )
            viewModel.insert(todo)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun updatePreviews() {
        imagePreviews.forEachIndexed { index, imageView ->
            if (index < selectedImageBytes.size) {
                // Hiển thị ảnh đã chọn
                val bitmap = BitmapFactory.decodeByteArray(
                    selectedImageBytes[index], 0, selectedImageBytes[index].size
                )
                imageView.setImageBitmap(bitmap)
            } else if (index == selectedImageBytes.size && selectedImageBytes.size < 3) {
                // Hiển thị icon thêm ảnh
                imageView.setImageResource(R.drawable.ic_add) // icon thêm
            } else {
                // Ẩn ImageView không dùng
                imageView.setImageDrawable(null)
            }
        }
    }



}
