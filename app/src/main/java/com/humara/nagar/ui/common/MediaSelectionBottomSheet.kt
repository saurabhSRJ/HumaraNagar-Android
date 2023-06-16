package com.humara.nagar.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humara.nagar.databinding.BottomSheetImagePickerBinding

class MediaSelectionBottomSheet : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "MediaSelectionBottomSheet"
        private var listener: MediaSelectionListener? = null
        fun show(fragmentManager: FragmentManager, listener: MediaSelectionListener) {
            this.listener = listener
            MediaSelectionBottomSheet().show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: BottomSheetImagePickerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetImagePickerBinding.inflate(inflater, container, false)
        isCancelable = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.run {
            llGallery.setOnClickListener {
                listener?.onGallerySelection()
                dismiss()
            }
            llCamera.setOnClickListener {
                listener?.onCameraSelection()
                dismiss()
            }
        }
    }
}

interface MediaSelectionListener {
    fun onCameraSelection()

    fun onGallerySelection()
}