package com.newrelic.instrumentation.okhttp3;

import com.newrelic.agent.android.TaskQueue;
import com.newrelic.agent.android.api.common.TransactionData;
import com.newrelic.agent.android.instrumentation.TransactionState;
import com.newrelic.agent.android.instrumentation.TransactionStateUtil;
import com.newrelic.agent.android.logging.AgentLog;
import com.newrelic.agent.android.logging.AgentLogManager;
import com.newrelic.agent.android.measurement.http.HttpTransactionMeasurement;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by muniraju on 01/03/16.
 */
public class CallExtension implements Call {
    private static final AgentLog log = AgentLogManager.getAgentLog();
    private TransactionState transactionState;
    private OkHttpClient client;
    private Request request;
    private Call impl;

    CallExtension(OkHttpClient client, Request request, Call impl) {
        this.client = client;
        this.request = request;
        this.impl = impl;
    }

    @Override
    public Request request() {
        return impl.request();
    }

    public Response execute() throws IOException {
        this.getTransactionState();
        Response response = null;

        try {
            response = this.impl.execute();
        } catch (IOException var3) {
            this.error(var3);
            throw var3;
        }

        return this.checkResponse(response);
    }

    public void enqueue(Callback responseCallback) {
        this.getTransactionState();
        this.impl.enqueue(new CallbackExtension(responseCallback, this.transactionState));
    }

    public void cancel() {
        this.impl.cancel();
    }

    @Override
    public boolean isExecuted() {
        return this.impl.isExecuted();
    }

    public boolean isCanceled() {
        return this.impl.isCanceled();
    }

    private Response checkResponse(Response response) {
        if(!this.getTransactionState().isComplete()) {
            response = OkHttp3TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), response);
        }

        return response;
    }

    private TransactionState getTransactionState() {
        if(this.transactionState == null) {
            this.transactionState = new TransactionState();
            OkHttp3TransactionStateUtil.inspectAndInstrument(this.transactionState, this.request);
        }

        return this.transactionState;
    }

    private void error(Exception e) {
        TransactionState transactionState = this.getTransactionState();
        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
        if(!transactionState.isComplete()) {
            TransactionData transactionData = transactionState.end();
            if(transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(),
                        transactionData.getHttpMethod(), transactionData.getStatusCode(),
                        transactionData.getErrorCode(), transactionData.getTimestamp(),
                        (double)transactionData.getTime(), transactionData.getBytesSent(),
                        transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }

    }
}
