package com.bigbasket.mobileapp.util;


import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ResponseSerializer {

    public static byte[] serializeObject(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.close();

            // Get the bytes of the serialized object
            byte[] buf = bos.toByteArray();

            return buf;
        } catch (IOException e) {
            Log.e("serializeObject", "error", e);

            return null;
        }
    }


    public static Object deserializeObject(byte[] b) {
        if (b == null) return null;
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = in.readObject();
            in.close();

            return object;
        } catch (ClassNotFoundException | IOException cnfe) {
            Log.e("deserializeObject", "class not found error", cnfe);

            return null;
        }
    }

}
