package fr.bouyguestelecom.tv.bridge.customer;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by rmessara on 01/02/17.
 * customer-profiling new
 */

public class MyBboxNotFoundException extends Exception {

    public MyBboxNotFoundException() {
        super("No bbox found! Check if a Miami bbox is present in the same network and if the service BboxApi is running.");
    }

    public void toast(Context context) {
        Toast toast = Toast.makeText(context,
                "No Bbox found. Please make sure the box is on the same network with the service BboxApi running.",
                Toast.LENGTH_LONG);
        toast.show();
    }
}