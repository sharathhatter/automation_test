package com.newrelic.instrumentation.okhttp3;

import com.newrelic.agent.android.Agent;
import com.newrelic.agent.android.Measurements;
import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;

import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by muniraju on 01/03/16.
 */
public class OkHttp3TransactionStateUtil {
    private static final AgentLog log = AgentLogManager.getAgentLog();
    private static final String NO_BODY_TEXT = "Response BODY not found.";

    public OkHttp3TransactionStateUtil() {
    }

    public static void inspectAndInstrument(TransactionState transactionState, Request request) {
        //TransactionStateUtil.inspectAndInstrument(transactionState, request.url().toString(), request.method());
        transactionState.setUrl(request.url().toString());
        transactionState.setHttpMethod(request.method());
        transactionState.setCarrier(Agent.getActiveNetworkCarrier());
        transactionState.setWanType(Agent.getActiveNetworkWanType());
    }

    public static Response inspectAndInstrumentResponse(TransactionState transactionState, Response response) {
        String appData = response.header("X-NewRelic-App-Data");
        int statusCode = response.code();
        long contentLength = 0L;

        try {
            contentLength = response.body().contentLength();
        } catch (Exception var7) {
            log.warning("Missing body or content length ");
        }

        //TransactionStateUtil.inspectAndInstrumentResponse(transactionState, appData, (int)contentLength, statusCode);
        if(appData != null && !appData.equals("")) {
            transactionState.setAppData(appData);
        }

        if(contentLength >= 0) {
            transactionState.setBytesReceived((long)contentLength);
        }

        transactionState.setStatusCode(statusCode);
        return addTransactionAndErrorData(transactionState, response);
    }

    private static Response addTransactionAndErrorData(TransactionState transactionState, Response response) {
        TransactionData transactionData = transactionState.end();
        if(transactionData != null) {
            TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(),
                    transactionData.getHttpMethod(), transactionData.getStatusCode(),
                    transactionData.getErrorCode(), transactionData.getTimestamp(),
                    (double)transactionData.getTime(), transactionData.getBytesSent(),
                    transactionData.getBytesReceived(), transactionData.getAppData()));
            if((long)transactionState.getStatusCode() >= 400L) {
                String contentTypeHeader = response.header("Content-Type");
                Object contentType = null;
                TreeMap params = new TreeMap();
                if(contentTypeHeader != null && contentTypeHeader.length() > 0 && !"".equals(contentTypeHeader)) {
                    params.put("content_type", contentType);
                }

                params.put("content_length", transactionState.getBytesReceived() + "");
                String responseBodyString = "";

                try {
                    final ResponseBody e = response.body();
                    responseBodyString = e.string();
                    final Buffer contents = (new Buffer()).write(responseBodyString.getBytes());
                    ResponseBody responseBody = new PrebufferedResponseBody(e, contents);
                    response = response.newBuilder().body(responseBody)
                            .request(response.request())
                            .protocol(response.protocol())
                            .code(response.code())
                            .message(response.message())
                            .headers(response.headers())
                            .handshake(response.handshake())
                            .cacheResponse(response.cacheResponse())
                            .priorResponse(response.priorResponse())
                            .networkResponse(response.networkResponse()).build();
                } catch (Exception var10) {
                    if(response.message() != null) {
                        log.warning("Missing response body, using response message");
                        responseBodyString = response.message();
                    }
                }

                Measurements.addHttpError(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), responseBodyString, params);
            }
        }

        return response;
    }
}