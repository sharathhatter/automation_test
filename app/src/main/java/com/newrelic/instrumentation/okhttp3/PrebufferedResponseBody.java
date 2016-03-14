package com.newrelic.instrumentation.okhttp3;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Created by muniraju on 01/03/16.
 */
public class PrebufferedResponseBody extends ResponseBody {
    private ResponseBody impl;
    private BufferedSource source;

    public PrebufferedResponseBody(ResponseBody impl, BufferedSource source) {
        this.impl = impl;
        this.source = source;
    }

    public MediaType contentType() {
        return this.impl.contentType();
    }

    public long contentLength() {
        return this.source.buffer().size();
    }

    public BufferedSource source() {
        return this.source;
    }

    public void close() {
        this.impl.close();
    }
}
