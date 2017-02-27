package fr.bouyguestelecom.tv.bridge.customer;

import java.util.TimerTask;

/**
 * Created by rmessara on 07/02/17.
 * customer-profiling new
 */

public class sampleTask extends TimerTask{
    Runnable myThreadObj;

    sampleTask (Runnable t){
        this.myThreadObj=t;
    }
    public void run() {
        myThreadObj.run();
    }
}
