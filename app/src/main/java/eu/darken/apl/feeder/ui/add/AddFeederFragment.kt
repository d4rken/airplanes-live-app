package eu.darken.apl.feeder.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
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
@OptIn(ExperimentalGetImage::class)
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

        ui.addButton.setOnClickListener {
            val feederId = ui.feederId.text.toString()
            val feederLabel = ui.feederLabel.text.toString()

            if (feederId.isBlank()) {
                ui.feederId.error = "Feeder ID is required"
                return@setOnClickListener
            }

            vm.addFeeder(
                label = feederLabel,
                rawId = feederId
            )

            findNavController().popBackStack()
        }

        ui.cancelButton.setOnClickListener { findNavController().popBackStack() }

        vm.state.observeWith(ui) { state ->
            if (feederId.text?.isBlank() == true) feederId.setText(state.receiverId)
            if (feederLabel.text?.isBlank() == true) feederLabel.setText(state.receiverLabel)
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

    private fun startCameraPreview() {
        if (isScanning) return
        isScanning = true

        cameraPreviewBinding = CameraPreviewLayoutBinding.inflate(layoutInflater)
        val cameraPreviewView = cameraPreviewBinding?.root ?: return

        ui.formContainer.visibility = View.GONE
        ui.root.addView(cameraPreviewView)

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
                Toast.makeText(requireContext(), "Error starting camera: ${e.message}", Toast.LENGTH_SHORT).show()
                stopCameraPreview()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCameraPreview() {
        if (!isScanning) return
        isScanning = false

        cameraProvider?.unbindAll()

        cameraPreviewBinding?.let { binding -> ui.root.removeView(binding.root) }

        ui.formContainer.visibility = View.VISIBLE

        cameraPreviewBinding = null
    }

    private inner class QrCodeAnalyzer : ImageAnalysis.Analyzer {
        @androidx.annotation.OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            try {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    barcodeScanner?.process(image)?.addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            for (barcode in barcodes) {
                                val rawValue = barcode.rawValue
                                if (rawValue == null) continue

                                log(TAG) { "QR code detected: $rawValue" }
                                if (!rawValue.startsWith(NewFeederQR.PREFIX)) continue

                                vm.handleQrScan(rawValue)

                                requireActivity().runOnUiThread { stopCameraPreview() }

                                break
                            }
                        }
                    }
                        ?.addOnFailureListener { e ->
                            log(TAG) { "Barcode scanning failed: ${e.message}" }
                        }
                        ?.addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
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
