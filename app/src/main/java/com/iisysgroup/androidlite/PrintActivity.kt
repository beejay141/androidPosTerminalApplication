package com.iisysgroup.androidlite

import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.iisysgroup.androidlite.models.ReceiptModel
import com.iisysgroup.androidlite.utils.PrintUtils
//import com.iisysgroup.androidlite.utils.PrintUtils.printBitmap
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import com.iisysgroup.poslib.deviceinterface.Printer
import com.iisysgroup.poslib.deviceinterface.printer.PrinterState
import com.pax.PaxDevice
import com.pax.dal.IPrinter
//import com.pos.device.printer.PrintCanvas
//import com.pos.device.printer.PrintTask
//import com.pos.device.printer.Printer
//import com.pos.device.printer.PrinterCallback
import kotlinx.android.synthetic.main.activity_print.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast
import java.lang.Exception


class PrintActivity : AppCompatActivity() {

    enum class VasType {
       AIRTEL_VTU, AIRTEL_DATA, MTN_VTU, MTN_DATA, GLO_VTU, GLO_DATA, ETISALAT_VTU, ETISALAT_DATA,
        SMILE_TOP_UP, SMILE_DATA_PURCHASE, DSTV, GOTV, EKEDC_PREPAID, EKEDC_POSTPAID, IKEDC_PREPAID,
        IKEDC_POSTPAID, NOT_INCLUDED, PURCHASE,AEDC,EEDC,PHEDC,IBEDC,GENESIS, STARTIMES, ITEX
    }


    val TAG = "MYDEBUG"
    val PRINTER_WIDTH = 384
    var merchantName : Any = ""
//    private lateinit var printTask: PrintTask

    object KEYS {
        const val PRINT_RECEIPT_VAS_TYPE = "vas_type"
        const val PRINT_RECEIPT_MODEL_KEY = "print_map"

        const val PRINT_RECEIPT_RECEIPT_OWNER = "receipt_owner"
        const val PRINT_RECEIPT_TERMINAL_ID = "receipt_terminal_id"
    }

    private var vasType : VasType?  = null

    private val printMap by lazy {
        intent.getSerializableExtra(KEYS.PRINT_RECEIPT_MODEL_KEY) as ReceiptModel
    }

    private val configData by lazy {
        (application as App).db.configDataDao
    }

    private val terminalId by lazy {
        if (intent.hasExtra(KEYS.PRINT_RECEIPT_TERMINAL_ID)){
            intent.getStringExtra(KEYS.PRINT_RECEIPT_TERMINAL_ID)
        } else {
            SharedPreferenceUtils.getTerminalId(this)
        }

    }

    private val isOneTimePrint by lazy {
        intent.hasExtra(KEYS.PRINT_RECEIPT_RECEIPT_OWNER)
    }

    lateinit var skDevice : PaxDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        val receipt = findViewById<LinearLayout>(R.id.receipt)
        skDevice = PaxDevice(this);
        launch {
            try {

                merchantName = configData.get().getConfigData("52040")
            }
            catch (e : Exception){}

            launch(UI){
                receipt.merchantName.text = merchantName.toString()


                if (intent.hasExtra(KEYS.PRINT_RECEIPT_VAS_TYPE)){
                    vasType = intent.getSerializableExtra(KEYS.PRINT_RECEIPT_VAS_TYPE) as VasType

                    if (vasType == VasType.PURCHASE){
                        val drawable = PrintUtils.getDrawableFromTerminalId(this@PrintActivity, terminalId)
                        receipt.bankImage.setImageDrawable(drawable)
                    } else {
                        val drawable = PrintUtils.getDrawableFromVasType(this@PrintActivity, vasType)
                        receipt.bankImage.setImageDrawable(drawable)
                    }
                } else {
                    vasType = VasType.NOT_INCLUDED
                    val drawable = getDrawable(R.drawable.itex_logo)
                    receipt.bankImage.setImageDrawable(drawable)
                }

                if (isOneTimePrint){
                    receipt.receiptOwner.text = intent.getStringExtra(KEYS.PRINT_RECEIPT_RECEIPT_OWNER)
                    val view = PrintUtils.generateReceipt(this@PrintActivity, printMap, receipt)

                    view.post {
                        val bitmap = getBitmapFromView(view)
                        printBitmap(bitmap, true)
                    }
                } else {
                    receipt.receiptOwner.text = "Customer's copy"

                    val view = PrintUtils.generateReceipt(this@PrintActivity, printMap, receipt)
                    view.post {
                        val bitmap = getBitmapFromView(view)
                        printBitmap(bitmap, true)
                        alert {
                            title = "Merchant's copy"
                            message = "Press OK to print Merchant's copy"
                            okButton {
                                receipt.receiptOwner.text = "Merchant's copy"
                                val bitmap = getBitmapFromView(view)
                                printBitmap(bitmap, true)
                            }
                            cancelButton {

                            }
                        }.show()
                    }
                }
            }
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null)
        //has background drawable , then draw it on the canvas
            bgDrawable.draw(canvas)
        else
        //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }


    private fun printBitmap(bitmap: Bitmap?, fitToPage: Boolean) {
       skDevice.StartPrint(bitmap, {
           when{
               it == PrinterState.OK ->{
                   toast("Printed done")
               }
           }
       });
    }

    private fun scaleBitmap(img: Bitmap, scale: Int): Bitmap {
        return Bitmap.createScaledBitmap(img, img.width * scale, img.height * scale, true)
    }

//    private val printerCallback = PrinterCallback { arg0, arg1 ->
//
//    }

}
