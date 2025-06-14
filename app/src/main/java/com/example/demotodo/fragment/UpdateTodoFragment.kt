package com.example.demotodo.fragment

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.demotodo.DAO.TodoDAO
import com.example.demotodo.R
import com.example.demotodo.TodoActivity
import com.example.demotodo.adapter.ImagePagerAdapter
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.model.Todo
import kotlinx.coroutines.launch

class UpdateTodoFragment : Fragment() {
    private lateinit var todo: Todo
    private lateinit var todoDao: TodoDAO
    private var selectedImageBytes = mutableListOf<ByteArray>()
    private var imagePreviews = mutableListOf<ImageView>()

    private var updateImageIndex: Int = -1
    private var isUpdatingImage = false


    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            val remainingSlots = 3 - selectedImageBytes.size
            val imagesToAdd = it.take(remainingSlots)
            for (uri in imagesToAdd) {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    selectedImageBytes.add(bytes)
                }
            }
            updateImagePreViews()
        }
    }
    private val updateImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = requireContext().contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null && updateImageIndex in selectedImageBytes.indices) {
                selectedImageBytes[updateImageIndex] = bytes
                updateImagePreViews()
            }
        }
    }

    private fun updateImagePreViews() {
        imagePreviews.forEachIndexed { index, imageView ->
            if (index < selectedImageBytes.size) {
                val bitmap = BitmapFactory.decodeByteArray(
                    selectedImageBytes[index],
                    0,
                    selectedImageBytes[index].size
                )
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE
            } else if (index == selectedImageBytes.size && index < 3) {
                imageView.setImageResource(R.drawable.ic_add)
                imageView.visibility = View.VISIBLE
                imageView.setOnClickListener {
                    val remainingSlots = 3 - selectedImageBytes.size
                    if (remainingSlots > 0) {
                        isUpdatingImage = false
                        imagePickerLauncher.launch("image/*")
                    }
                }
            } else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }

        val previewLayout = view?.findViewById<LinearLayout>(R.id.img_preview)
        if (selectedImageBytes.isNotEmpty()) {
            previewLayout?.setBackgroundResource(0)
        }
    }

    private var todoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        todoDao = db.todoDAO()
        todoId = arguments?.getInt("todo_id") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_infor_detail, container, false)
        val edtTitle = view.findViewById<EditText>(R.id.edt_title)
        val edtContent = view.findViewById<EditText>(R.id.edt_content)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val imgPre = view.findViewById<LinearLayout>(R.id.img_preview)

        fetchTodo(todoId) { fetchedTodo ->
            if (fetchedTodo != null) {
                todo = fetchedTodo
                edtTitle.setText(todo.title)
                edtContent.setText(todo.content)
                selectedImageBytes = todo.imageData.toMutableList()

                imagePreviews.clear()
                imgPre.removeAllViews()

                repeat(3) { index ->
                    val imageView = ImageView(requireContext())
                    imageView.layoutParams = LinearLayout.LayoutParams(
                        requireContext().resources.getDimensionPixelSize(R.dimen.img_with),
                        requireContext().resources.getDimensionPixelSize(R.dimen.img_height)
                    ).apply {
                        setMargins(8, 8, 8, 8)
                    }
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.visibility = View.GONE
                    imageView.tag = index

                    imageView.setOnClickListener {
                        val clickedIndex = it.tag as Int
                        if (clickedIndex < selectedImageBytes.size) {
                            showImageDialog(clickedIndex)
                        } else if (clickedIndex == selectedImageBytes.size && clickedIndex < 3) {
                            val remaining = 3 - selectedImageBytes.size
                            if (remaining > 0) {
                                isUpdatingImage = false
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                    }

                    imagePreviews.add(imageView)
                    imgPre.addView(imageView)
                }

                updateImagePreViews()
            }
        }

        btnBack.setOnClickListener {
            val title = edtTitle.text.toString()
            val content = edtContent.text.toString()
            if (title.isEmpty() && content.isEmpty()) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                todo.title = title
                todo.content = content
                todo.imageData = selectedImageBytes.toList()
                todoDao.updateTodo(todo)
                Toast.makeText(requireContext(), "Đã cập nhật ghi chú!", Toast.LENGTH_SHORT).show()
                val activity = activity as? TodoActivity
                activity?.refreshList()
                requireActivity().supportFragmentManager.popBackStack()
                activity?.findViewById<RecyclerView>(R.id.rv_todo)?.visibility = View.VISIBLE
                activity?.findViewById<ImageView>(R.id.btn_add)?.visibility = View.VISIBLE
                activity?.findViewById<FrameLayout>(R.id.fragment_container)?.visibility = View.GONE
                activity?.recreate()
            }
        }

        return view
    }

    private fun fetchTodo(todoId: Int, callback: (Todo?) -> Unit) {
        lifecycleScope.launch {
            val fetchedTodo = todoDao.getTodoById(todoId)
            callback(fetchedTodo)
        }
    }

    private fun showImageDialog(index: Int) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.layout_fullscreen_image_dialog)
        val viewPager = dialog.findViewById<ViewPager2>(R.id.viewpager)
        val btnUpdate = dialog.findViewById<ImageView>(R.id.img_edit)
        val btnDelete = dialog.findViewById<ImageView>(R.id.img_delete)
        val btnBack = dialog.findViewById<ImageView>(R.id.img_back)

        viewPager.adapter = ImagePagerAdapter(selectedImageBytes)
        viewPager.setCurrentItem(index, false)

        btnDelete.setOnClickListener {
            val current = viewPager.currentItem
            selectedImageBytes.removeAt(current)
            updateImagePreViews()
            dialog.dismiss()
        }

        btnUpdate.setOnClickListener {
            val current = viewPager.currentItem
            dialog.dismiss()
            updateImage(current)
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateImage(index: Int) {
        isUpdatingImage = true
        updateImageIndex = index
        updateImageLauncher.launch("image/*")
    }
}
