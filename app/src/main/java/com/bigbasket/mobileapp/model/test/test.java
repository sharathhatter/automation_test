package com.bigbasket.mobileapp.model.test;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Created by jugal on 11/9/14.
 */
public class test {

    Object[] arguments = {
            Integer.valueOf(7), new Date(System.currentTimeMillis()),
            "a disturbance in the Force"};
    String result = MessageFormat.format(
            "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
            arguments);
}
