package com.iisysgroup.androidlite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//import com.pos.device.printer.PrintCanvas;
//import com.pos.device.printer.PrintTask;
//import com.pos.device.printer.Printer;
//import com.pos.device.printer.PrinterCallback;

public class PrintTesting extends Activity {

    public static final String TAG="MYDEBUG";
    Button printimage;
    public static final int PRINTER_WIDTH=384;
//    private PrintTask printTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_testing);
        printimage = (Button) findViewById(R.id.printimage);

        printimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ampreceipt);
                printBitmap(tempBitmap, true);
            }
        });

    }


    void printBitmap(Bitmap bitmap, boolean fitToPage){
        if (bitmap==null || bitmap.getWidth()==0 || bitmap.getHeight()==0){
            return;
        }

        Log.d(TAG, "original Bitmap Width:"+bitmap.getWidth()+"   Height:"+bitmap.getHeight()+"  FitToPage="+fitToPage);

        // PrintTask constructor
//        printTask=new PrintTask();

        // PrintCanvas constructor
//        PrintCanvas printCanvas=new PrintCanvas();
        Paint paint=new Paint();




//        if (fitToPage && bitmap.getWidth()!=PRINTER_WIDTH){
//            int scaledHeight=(PRINTER_WIDTH * bitmap.getHeight() )/ bitmap.getWidth();
//            Bitmap scaledBitmap= Bitmap.createScaledBitmap(bitmap, PRINTER_WIDTH, scaledHeight, false);
//            Log.d(TAG, "scaled Bitmap Width:"+scaledBitmap.getWidth()+"   Height:"+scaledBitmap.getHeight());
//            // Draw the bitmap
//            printCanvas.drawBitmap(scaledBitmap, paint);
//        }
//        else{
//            // Draw the bitmap
//            printCanvas.drawBitmap(bitmap, paint);
//        }
//        // Set print canvas
//        printTask.setPrintCanvas(printCanvas);
//        // Set the amount of feed paper
//        printTask.addFeedPaper(100);
//        // Get the gray value of the task
//        printTask.setGray(130);
//
//        // Start print task
//        Printer.getInstance().startPrint(printTask, printerCallback);
    }

//    private PrinterCallback printerCallback=new PrinterCallback() {
//
//        @Override
//        public void onResult(int arg0, PrintTask arg1) {
//            Log.d(TAG, "Printer Callback onResult");
//            String str="Printer Callback onResult "+arg0+"   Gray: "+arg1.getGray();
//            Log.d(TAG, str);
//            toastOnUI(str);
//        }
//    };

    private void toastOnUI(final String str){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PrintTesting.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
