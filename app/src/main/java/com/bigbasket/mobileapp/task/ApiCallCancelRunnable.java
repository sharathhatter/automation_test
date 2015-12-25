package com.bigbasket.mobileapp.task;

import retrofit.Call;

/**
 * Created by muniraju on 17/12/15.
 */
public class ApiCallCancelRunnable implements Runnable {

    private final Call<?> call;

    public ApiCallCancelRunnable(Call<?> call){
        this.call = call;
    }

    @Override
    public void run() {
        if(call == null) {
            return;
        }
        try {
            call.cancel();
        } catch (Throwable t){
            //Ignore
        }
    }
}
