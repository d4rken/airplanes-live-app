package eu.darken.apl.feeder.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.debug.logging.log
import eu.darken.apl.common.debug.logging.logTag
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.common.viewbinding.viewBinding
import eu.darken.apl.databinding.AddFeederFragmentBinding
import eu.darken.apl.databinding.CameraPreviewLayoutBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class AddFeederFragment : Fragment3(R.layout.add_feeder_fragment) {

    override val vm: AddFeederViewModel by viewModels()
    override val ui: AddFeederFragmentBinding by viewBinding()

    private var cameraPreviewBinding: CameraPreviewLayoutBinding? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var isScanning = false

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCameraPreview()
        } else {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(getString(R.string.feeder_list_camera_permission_required_title))
                setMessage(getString(R.string.feeder_list_camera_permission_required_message))
                setPositiveButton(R.string.common_done_action) { dialog, _ -> dialog.dismiss() }
            }.show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cameraExecutor = Executors.newSingleThreadExecutor()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        ui.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        ui.scanQrButton.setOnClickListener { startQrCodeScanning() }

        ui.detectLocalButton.setOnClickListener {
            vm.detectLocalFeeder()
        }

        ui.feederId.doAfterTextChanged { text ->
            vm.updateReceiverId(text.toString())
        }
        ui.feederLabel.doAfterTextChanged { text ->
            vm.updateReceiverLabel(text.toString())
        }
        ui.feederIpAddress.doAfterTextChanged { text ->
            vm.updateReceiverIpAddress(text.toString())
        }
        ui.feederPosition.doAfterTextChanged { text ->
            vm.updateReceiverPosition(text.toString())
        }

        ui.addButton.setOnClickListener {
            vm.addFeeder()
        }

        vm.state.observeWith(ui) { state ->
            if (feederId.text.toString() != state.receiverId) {
                feederId.setText(state.receiverId)
            }
            if (feederLabel.text.toString() != state.receiverLabel) {
                feederLabel.setText(state.receiverLabel)
            }
            if (feederIpAddress.text.toString() != state.receiverIpAddress) {
                feederIpAddress.setText(state.receiverIpAddress)
            }
            if (feederPosition.text.toString() != state.receiverPosition) {
                feederPosition.setText(state.receiverPosition)
            }

            // Handle loading state
            val isEnabled = !state.isLoading && !state.isDetectingLocal
            feederId.isEnabled = isEnabled
            feederLabel.isEnabled = isEnabled
            feederIpAddress.isEnabled = isEnabled
            feederPosition.isEnabled = isEnabled
            scanQrButton.isEnabled = isEnabled
            detectLocalButton.isEnabled = isEnabled

            // Handle button and progress indicator
            if (state.isLoading) {
                addButton.visibility = View.INVISIBLE
                progressIndicator.visibility = View.VISIBLE
                progressIndicator.show()
            } else {
                addButton.visibility = View.VISIBLE
                progressIndicator.hide()
                progressIndicator.visibility = View.GONE
                addButton.isEnabled = state.isAddButtonEnabled
            }

            // Handle local detection button state
            if (state.isDetectingLocal) {
                detectLocalButton.text = getString(R.string.feeder_list_detecting_local_feeder)
            } else {
                detectLocalButton.text = getString(R.string.feeder_list_detect_local_title)
            }
        }

        vm.events.observeWith(ui) { event ->
            when (event) {
                is AddFeederEvents.StopCamera -> stopCameraPreview()
                is AddFeederEvents.ShowLocalDetectionResult -> {
                    val message = when (event.result) {
                        LocalDetectionResult.FOUND -> getString(R.string.feeder_list_local_feeder_found)
                        LocalDetectionResult.NOT_FOUND -> getString(R.string.feeder_list_no_local_feeder_found)
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
        barcodeScanner?.close()
        cameraProvider?.unbindAll()
        cameraPreviewBinding = null
        super.onDestroyView()
    }

    private fun startQrCodeScanning() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCameraPreview()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.feeder_list_camera_permission_required_title)
                    setMessage(R.string.feeder_list_camera_permission_required_message)
                    setPositiveButton(R.string.common_grant_permission_action) { _, _ ->
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    setNegativeButton(R.string.common_cancel_action) { dialog, _ -> dialog.dismiss() }
                }.show()
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCameraPreview() {
        if (isScanning) return
        isScanning = true

        cameraPreviewBinding = CameraPreviewLayoutBinding.inflate(layoutInflater)
        val cameraPreviewView = cameraPreviewBinding?.root ?: return

        // Hide everything except toolbar
        ui.formContainer.visibility = View.GONE
        ui.buttonContainer.visibility = View.GONE

        // Add camera preview as full screen overlay
        val layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
        ).apply {
            topToBottom = ui.toolbar.id
            bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        }

        ui.root.addView(cameraPreviewView, layoutParams)

        cameraPreviewBinding?.closeButton?.setOnClickListener {
            stopCameraPreview()
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(cameraPreviewBinding?.previewView?.surfaceProvider)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer())

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                log(TAG) { "Error starting camera: ${e.message}" }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.feeder_list_camera_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
                stopCameraPreview()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCameraPreview() {
        if (!isScanning) return
        isScanning = false
        cameraProvider?.unbindAll()
        cameraPreviewBinding?.let { binding -> ui.root.removeView(binding.root) }

        // Restore all UI elements
        ui.formContainer.visibility = View.VISIBLE
        ui.buttonContainer.visibility = View.VISIBLE
        cameraPreviewBinding = null
    }

    @ExperimentalGetImage
    private inner class QrCodeAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            try {
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return
                }

                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                barcodeScanner?.process(image)
                    ?.addOnSuccessListener { barcodes ->

                        for (barcode in barcodes) {
                            val rawValue = barcode.rawValue
                            if (rawValue == null) continue

                            log(TAG) { "QR code detected: $rawValue" }
                            vm.handleQrScan(rawValue)

                            break
                        }
                    }
                    ?.addOnFailureListener { e -> log(TAG) { "Barcode scanning failed: ${e.message}" } }
                    ?.addOnCompleteListener { imageProxy.close() }
            } catch (e: Exception) {
                log(TAG) { "Error analyzing image: ${e.message}" }
                imageProxy.close()
            }
        }
    }

    companion object {
        private val TAG = logTag("Feeder", "Add", "Fragment")
    }
}