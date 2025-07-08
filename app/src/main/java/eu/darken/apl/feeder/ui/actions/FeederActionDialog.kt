package eu.darken.apl.feeder.ui.actions

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.hasApiLevel
import eu.darken.apl.common.permissions.Permission
import eu.darken.apl.common.uix.BottomSheetDialogFragment2
import eu.darken.apl.databinding.CommonTextinputDialogBinding
import eu.darken.apl.databinding.FeederActionDialogBinding
import eu.darken.apl.databinding.QrCodeDialogBinding
import eu.darken.apl.feeder.ui.add.NewFeederQR
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class FeederActionDialog : BottomSheetDialogFragment2() {
    override val vm: FeederActionViewModel by viewModels()
    override lateinit var ui: FeederActionDialogBinding

    @Inject lateinit var json: Json

    private val permissionlauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ui = FeederActionDialogBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.state.observeWith(ui) { (feeder) ->
            primary.text = feeder.label
            secondary.text = feeder.id
            tertiary.text = "Host-address: ${feeder.config.address ?: "\uD83C\uDF7B"}"
            monitorFeederOfflineToggle.isChecked = feeder.config.offlineCheckTimeout != null
            addressActions.isGone = feeder.config.address == null
        }

        ui.monitorFeederOfflineToggle.apply {
            setOnClickListener {
                if (hasApiLevel(33) && !Permission.POST_NOTIFICATIONS.isGranted(requireContext())) {
                    permissionlauncher.launch(Permission.POST_NOTIFICATIONS.permissionId)
                }
                vm.toggleNotifyWhenOffline()
            }
        }

        ui.showFeedAction.setOnClickListener { vm.showFeedOnMap() }
        ui.renameAction.setOnClickListener { vm.renameFeeder() }
        ui.setAddressAction.setOnClickListener { vm.changeAddress() }
        ui.generateQrIconAction.setOnClickListener { vm.generateQrCode() }
        ui.addressTar1090Action.setOnClickListener { vm.openTar1090() }
        ui.addressGraphs1090Action.setOnClickListener { vm.openGraphs1090() }
        ui.removeFeederAction.setOnClickListener { vm.removeFeeder() }


        vm.events.observeWith(ui) { event ->
            when (event) {
                is FeederActionEvents.RemovalConfirmation -> MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.feeder_remove_confirmation_title)
                    setMessage(R.string.feeder_remove_confirmation_message)
                    setPositiveButton(R.string.common_remove_action) { _, _ -> vm.removeFeeder(confirmed = true) }
                    setNegativeButton(R.string.common_cancel_action) { _, _ -> }
                }.show()

                is FeederActionEvents.Rename -> MaterialAlertDialogBuilder(requireContext()).apply {
                    val layout = CommonTextinputDialogBinding.inflate(layoutInflater, null, false).apply {
                        input.hint = "T-EDKA123"
                        input.setText(event.feeder.label)
                    }
                    setTitle(R.string.feeder_name_change_title)
                    setMessage(R.string.feeder_name_change_body)
                    setView(layout.root)
                    setPositiveButton(R.string.feeder_name_change_action) { _, _ ->
                        vm.renameFeeder(layout.input.text.toString())
                    }
                    setNegativeButton(R.string.common_cancel_action) { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()

                is FeederActionEvents.ChangeIpAddress -> MaterialAlertDialogBuilder(requireContext()).apply {
                    val layout = CommonTextinputDialogBinding.inflate(layoutInflater, null, false).apply {
                        input.hint = "192.168.0.42/myfeeder.domain.tld"
                        input.setText(event.feeder.config.address)
                    }
                    setTitle(R.string.feeder_address_change_title)
                    setMessage(R.string.feeder_address_change_body)
                    setView(layout.root)
                    setPositiveButton(R.string.feeder_address_change_action) { _, _ ->
                        vm.changeAddress(layout.input.text.toString())
                    }
                    setNegativeButton(R.string.common_cancel_action) { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()

                is FeederActionEvents.ShowQrCode -> showQrCodeDialog(event.qr)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun showQrCodeDialog(qr: NewFeederQR) {
        try {
            val qrCodeText = qr.toUri(json).toString()
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(qrCodeText, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = createBitmapFromBitMatrix(bitMatrix)

            val dialogBinding = QrCodeDialogBinding.inflate(layoutInflater, null, false)
            dialogBinding.qrCodeImage.setImageBitmap(bitmap)

            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .create()

            dialogBinding.shareButton.setOnClickListener {
                shareQrCode(bitmap, qr)
            }

            dialogBinding.closeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        } catch (e: WriterException) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.common_error_label)
                .setMessage("Failed to generate QR code: ${e.message}")
                .setPositiveButton(R.string.common_close_action) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun createBitmapFromBitMatrix(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bitmap
    }

    private fun shareQrCode(bitmap: Bitmap, qr: NewFeederQR) {
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()

            val fileName = "feeder_qr_${qr.receiverId}_${System.currentTimeMillis()}.png"
            val file = File(cachePath, fileName)

            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "Feeder QR Code: ${qr.receiverLabel ?: qr.receiverId}")
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.common_share_action)))

        } catch (e: IOException) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.common_error_label)
                .setMessage("Failed to share QR code: ${e.message}")
                .setPositiveButton(R.string.common_close_action) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}