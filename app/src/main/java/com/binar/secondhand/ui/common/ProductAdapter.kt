package com.binar.secondhand.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.binar.secondhand.data.source.local.entity.BuyerProductEntity
import com.binar.secondhand.databinding.ListItemProductBinding
import com.binar.secondhand.utils.currencyFormatter
import com.binar.secondhand.utils.ui.loadPhotoUrl

class ProductAdapter(
    private var onDetailClick: (BuyerProductEntity) -> Unit
) : PagingDataAdapter<BuyerProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            ListItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.binding.apply {
            product?.let {
                it.imageUrl?.let {
                    productImage.loadPhotoUrl(it)
                }
                productNameTv.text = it.name
                locationTv.text = it.location
                it.basePrice?.let { basePrice ->
                    priceTv.text = "Rp. ${basePrice.currencyFormatter()}"
                }
                root.setOnClickListener {
                    onDetailClick(product)
                }
            }
        }
    }

    class ProductViewHolder(
        val binding: ListItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root)

    object ProductDiffCallBack: DiffUtil.ItemCallback<BuyerProductEntity>() {
        override fun areItemsTheSame(
            oldItem: BuyerProductEntity,
            newItem: BuyerProductEntity
        ): Boolean {
            return oldItem.buyerProductId == newItem.buyerProductId
        }

        override fun areContentsTheSame(
            oldItem: BuyerProductEntity,
            newItem: BuyerProductEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}