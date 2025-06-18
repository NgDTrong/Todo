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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.demotodo.DAO.TodoDAO
import com.example.demotodo.R
import com.example.demotodo.TodoActivity
import com.example.demotodo.adapter.ImagePagerAdapter
import com.example.demotodo.data.AppDatabase
import com.example.demotodo.databinding.ActivityInforDetailBinding
import com.example.demotodo.databinding.LayoutFullscreenImageDialogBinding
import com.example.demotodo.model.Todo
import com.example.demotodo.viewmodel.UpdateTodoViewModel
import com.example.demotodo.viewmodel.UpdateTodoViewModelFactory
import kotlinx.coroutines.launch
class UpdateTodoFragment : Fragment() {

    private var _binding: ActivityInforDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UpdateTodoViewModel
    private var selectedImageBytes = mutableListOf<ByteArray>()
    private var imagePreviews = mutableListOf<ImageView>()

    private var updateImageIndex = -1
    private var isUpdatingImage = false
    private var todoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        val factory = UpdateTodoViewModelFactory(db.todoDAO())
        viewModel = ViewModelProvider(this, factory)[UpdateTodoViewModel::class.java]

        todoId = arguments?.getInt("todo_id") ?: -1
        if (todoId != -1) {
            viewModel.loadTodoById(todoId)
        } else {
            Toast.makeText(requireContext(), "Todo không hợp lệ", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityInforDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe ViewModel
        viewModel.todo.observe(viewLifecycleOwner) { todo ->
            todo?.let {
                binding.edtTitle.setText(it.title)
                binding.edtContent.setText(it.content)
                selectedImageBytes = it.imageData.toMutableList()

                setupImagePreview()
                updateImagePreviews()
            }
        }

        binding.btnBack.setOnClickListener {
            val title = binding.edtTitle.text.toString()
            val content = binding.edtContent.text.toString()

            if (title.isEmpty() && content.isEmpty()) return@setOnClickListener

            viewModel.todo.value?.let {
                it.title = title
                it.content = content
                it.imageData = selectedImageBytes.toList()

                viewModel.updateTodo(it)
                Toast.makeText(requireContext(), "Đã cập nhật ghi chú!", Toast.LENGTH_SHORT).show()
                (activity as? TodoActivity)?.let { act ->
                    act.refreshList()
                    act.findViewById<RecyclerView>(R.id.rv_todo)?.visibility = View.VISIBLE
                    act.findViewById<ImageView>(R.id.btn_add)?.visibility = View.VISIBLE
                    act.findViewById<FrameLayout>(R.id.fragment_container)?.visibility = View.GONE
                }
                parentFragmentManager.setFragmentResult("todo_updated", Bundle().apply {
                    putBoolean("updated", true)
                })

                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun setupImagePreview() {
        imagePreviews.clear()
        binding.imgPreview.removeAllViews()

        repeat(3) { index ->
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.img_with),
                    resources.getDimensionPixelSize(R.dimen.img_height)
                ).apply { setMargins(8, 8, 8, 8) }

                scaleType = ImageView.ScaleType.CENTER_CROP
                visibility = View.GONE
                tag = index

                setOnClickListener {
                    val clickedIndex = tag as Int
                    if (clickedIndex < selectedImageBytes.size) {
                        showImageDialog(clickedIndex)
                    } else {
                        isUpdatingImage = false
                        imagePickerLauncher.launch("image/*")
                    }
                }
            }
            imagePreviews.add(imageView)
            binding.imgPreview.addView(imageView)
        }
    }

    private fun updateImagePreviews() {
        imagePreviews.forEachIndexed { index, imageView ->
            if (index < selectedImageBytes.size) {
                try {
                    val bitmap = BitmapFactory.decodeByteArray(selectedImageBytes[index], 0, selectedImageBytes[index].size)
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                } catch (e: Exception) {

                }
            } else if (index == selectedImageBytes.size && index<3 ) {
                imageView.setImageResource(R.drawable.ic_add)
                imageView.visibility = View.VISIBLE
            } else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            val remaining = 3 - selectedImageBytes.size
            val toAdd = it.take(remaining)
            for (uri in toAdd) {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) selectedImageBytes.add(bytes)
            }
            updateImagePreviews()
        }
    }

    private val updateImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = requireContext().contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null && updateImageIndex in selectedImageBytes.indices) {
                selectedImageBytes[updateImageIndex] = bytes
                updateImagePreviews()
            }
        }
    }

    private fun showImageDialog(index: Int) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val bindingDialog = LayoutFullscreenImageDialogBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(bindingDialog.root)

        bindingDialog.viewpager.adapter = ImagePagerAdapter(selectedImageBytes)
        bindingDialog.viewpager.setCurrentItem(index, false)

        bindingDialog.imgDelete.setOnClickListener {
            val current = bindingDialog.viewpager.currentItem
            selectedImageBytes.removeAt(current)
            updateImagePreviews()
            dialog.dismiss()
        }

        bindingDialog.imgEdit.setOnClickListener {
            val current = bindingDialog.viewpager.currentItem
            dialog.dismiss()
            updateImage(current)
        }

        bindingDialog.imgBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateImage(index: Int) {
        isUpdatingImage = true
        updateImageIndex = index
        updateImageLauncher.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


