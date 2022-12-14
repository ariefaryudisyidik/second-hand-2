package com.binar.secondhand.ui.sellerorder

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.binar.secondhand.R
import com.binar.secondhand.base.BaseFragment
import com.binar.secondhand.databinding.FragmentSellerOrderBinding
import com.binar.secondhand.ui.bidderinfo.BidderInfoFragment
import com.binar.secondhand.ui.common.AuthViewModel
import com.binar.secondhand.utils.RequireAuthentication
import com.binar.secondhand.utils.logi
import com.binar.secondhand.utils.ui.setMargin
import com.binar.secondhand.utils.ui.showShortSnackbar
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SellerOrderFragment : BaseFragment(R.layout.fragment_seller_order) {
    override var requireAuthentication = true

    private val binding: FragmentSellerOrderBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RequireAuthentication.execute(authViewModel, navController, viewLifecycleOwner)

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
            tab.icon = ContextCompat.getDrawable(
                requireContext(), TAB_ICONS[position]
            )
        }.attach()

        binding.tabs.setMargin()
    }

    companion object {
        @StringRes
        val TAB_TITLES = intArrayOf(
            R.string.interested,
            R.string.sold
        )
        val TAB_ICONS = intArrayOf(
            R.drawable.ic_heart_tab_layout,
            R.drawable.ic_dollar_sign_tab_layout,
        )
    }
}