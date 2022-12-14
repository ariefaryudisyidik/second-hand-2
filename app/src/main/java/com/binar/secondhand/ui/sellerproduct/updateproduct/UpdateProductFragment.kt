package com.binar.secondhand.ui.sellerproduct.updateproduct

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.binar.secondhand.R
import com.binar.secondhand.base.BaseFragment
import com.binar.secondhand.data.Result
import com.binar.secondhand.data.source.remote.request.SellerProductRequest
import com.binar.secondhand.data.source.remote.response.CategoryResponse
import com.binar.secondhand.databinding.FragmentUpdateProductBinding
import com.binar.secondhand.ui.camera.CameraFragment
import com.binar.secondhand.utils.*
import com.binar.secondhand.utils.ui.loadPhotoUrl
import com.binar.secondhand.utils.ui.showShortSnackbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class UpdateProductFragment : BaseFragment(R.layout.fragment_update_product) {
    override var bottomNavigationViewVisibility = View.GONE

    private val binding: FragmentUpdateProductBinding by viewBinding()

    private val args: UpdateProductFragmentArgs by navArgs()
    private val categoryID = ArrayList<Int>()
    private var getFile: File? = null
    private var isImageFromGallery: Boolean = true
    private var isBackCamera: Boolean = false

    val viewModel by viewModel<UpdateProductViewmodel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = activity?.window
        window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val toolbar: MaterialToolbar = binding.materialToolbar2
        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }
        val id = args.id
        observeUI(id)
        setupObserver()
        binding.productImageView.setOnClickListener {
            chooseImageDialog()
        }

        binding.saveBtn.setOnClickListener {
            updateProduct(id)
        }

    }
    override fun onResume() {
        super.onResume()
        logd("isgalery => $isImageFromGallery")
        if (!isImageFromGallery){
            logd("isg")
            cameraResult()
        }
    }

    private fun observeUI(id: Int) {
        viewModel.getProductDetail(id).observe(viewLifecycleOwner) { product ->
            when (product) {
                is Result.Error -> {
                }
                Result.Loading -> {

                }
                is Result.Success -> {
                    binding.apply {
                        product.data.apply {
                            productNameEdt.setText(name)
                            productPriceEditText.setText(basePrice.toString())
                            if (categoryID.size == 0) {
                                for (i in categories){
                                    categoryID.add(i.id)
                                }
                                categoryEditText.setText(categories.joinToString {
                                    it.name
                                })
                            }
                            locationEdt.setText(location)
                            descriptionEdt.setText(description)
                            if (getFile == null) {
                                imageUrl?.let { productImageView.loadPhotoUrl(it) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupObserver() {
        viewModel.category.observe(viewLifecycleOwner) { item ->
            when (item) {
                is Result.Error -> {}
                Result.Loading -> {}
                is Result.Success -> {
                    binding.categoryEditText.setOnClickListener {
                        showMultipleChoicesAlert(item.data)
                    }

                }
            }
        }

    }

    private fun chooseImageDialog() {
        AlertDialog.Builder(requireActivity())
            .setMessage("choose Image")
            .setPositiveButton("Gallery") { _, _ ->
                isImageFromGallery = true
                startGallery() }
            .setNegativeButton("Camera") { _, _ ->
                isImageFromGallery = false
                navController.navigate(R.id.action_updateProductFragment_to_cameraFragment)
            }
            .show()
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            try {
                getFile = uriToFile(selectedImg, requireContext())
            } catch (e: Exception) {
                binding.root.showShortSnackbar("No such file or directory")
            }
            isImageFromGallery = true
            binding.productImageView.setImageURI(selectedImg)
        }
    }

    private fun updateProduct(id: Int) {
        if (categoryID.size != 0) {
            binding.apply {
                val productName = binding.productNameEdt.text.toString().createPartFromString()
                val productPrice =
                    binding.productPriceEditText.text.toString().createPartFromString()
                val productDescription =
                    binding.descriptionEdt.text.toString().createPartFromString()
                val category = categoryID.toString().createPartFromString()
                val location = binding.locationEdt.text.toString().createPartFromString()
                val map = HashMap<String, RequestBody>().apply {
                    put("name", productName)
                    put("base_price", productPrice)
                    put("description", productDescription)
                    put("category_ids", category)
                    put("location", location)
                }
                if (getFile != null) {
                    val file = reduceFileImage(getFile as File, isBackCamera, isImageFromGallery)
                    val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "image",
                        file.name,
                        requestImageFile
                    )
                    val sellerProductRequest = SellerProductRequest(
                        imageMultipart,
                        map
                    )
                    viewModel.doUpdateProduct(sellerProductRequest, id)
                        .observe(viewLifecycleOwner) { productResponse ->
                            when (productResponse) {
                                is Result.Error -> {
                                    view?.showShortSnackbar("Update Produk Gagal",false)
                                }
                                Result.Loading -> {

                                }
                                is Result.Success -> {
                                    view?.showShortSnackbar("Update Produk Berhasil")
                                    findNavController().navigate(UpdateProductFragmentDirections.actionUpdateProductFragmentToSellerProductFragment())
                                }
                            }
                        }

                } else {
                    val sellerProductRequest = SellerProductRequest(
                        file = null,
                        map
                    )
                    viewModel.doUpdateProduct(sellerProductRequest, id)
                        .observe(viewLifecycleOwner) { productResponse ->
                            when (productResponse) {
                                is Result.Error -> {
                                    view?.showShortSnackbar("Update Produk Gagal",false)
                                }
                                Result.Loading -> {

                                }
                                is Result.Success -> {
                                    view?.showShortSnackbar("Update Produk Berhasil")
                                    findNavController().navigate(UpdateProductFragmentDirections.actionUpdateProductFragmentToSellerProductFragment())
                                }
                            }
                        }
                }
            }
        } else {
            view?.showShortSnackbar("Pilih Minimal 1 Kategori",false)

        }
    }

    private fun showMultipleChoicesAlert(categoryResponse: List<CategoryResponse>) {
        val selectedList = ArrayList<Int>()
        val selectedItems = ArrayList<String>()
        var items = arrayOf<String>()
        var id = arrayOf<Int>()
        for (i in categoryResponse) {
            items = items.plus(i.name)
            id = id.plus(i.id)
        }
        MaterialAlertDialogBuilder(requireContext(), R.style.MyAlertDialogTheme)
            .setTitle("Pilih Kategori")
            .setMultiChoiceItems(items, null) { dialog, which, isChecked ->
                if (isChecked) {
                    selectedList.add(which)
                } else if (selectedList.contains(which)) {
                    selectedList.remove(which)
                }
            }
            .setPositiveButton("OK") { dialog, which ->
                categoryID.removeAll(categoryID)
                for (i in selectedList.indices) {
                    selectedItems.add(items[selectedList[i]])
                    categoryID.add(id[selectedList[i]])
                }
                binding.categoryEditText.setText(selectedItems.joinToString {
                    it
                })
                Log.d("id", "$categoryID")
            }
            .setNegativeButton("Batal") { dialog, which ->
                dialog.dismiss()
            }
            .setNeutralButton("Bersihkan Pilihan") { ialog, which ->
                selectedItems.removeAll(selectedItems)
                categoryID.removeAll(categoryID)
                binding.categoryEditText.setText(selectedItems.joinToString {
                    it
                })
            }
            .show()
    }
    private fun cameraResult(){
        val navController = navController
        val navBackStackEntry = navController.getBackStackEntry(R.id.updateProductFragment)
        if(navBackStackEntry.savedStateHandle.contains(CameraFragment.RESULT_KEY)){
            val result = navBackStackEntry.savedStateHandle.get<Bundle>(CameraFragment.RESULT_KEY)
            val myFile = result?.getSerializable("picture") as File
            isBackCamera = result.getBoolean("isBackCamera", true)

            getFile = myFile
            val resultFile = rotateBitmap(
                BitmapFactory.decodeFile(getFile?.path),
                isBackCamera
            )
            binding.productImageView.setImageBitmap(resultFile)
        }

    }
}