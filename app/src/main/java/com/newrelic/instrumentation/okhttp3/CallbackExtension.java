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
import okhttp3.Response;

/**
 * Created by muniraju on 01/03/16.
 */
public class CallbackExtension implements Callback {

    private static final AgentLog log = AgentLogManager.getAgentLog();
    private TransactionState transactionState;
    private Callback impl;

    public CallbackExtension(Callback impl, TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if(log.getLevel() >= AgentLog.VERBOSE) {
            log.verbose("CallbackExtension.onFailure() - logging error.");
        }

        this.error(e);
        this.impl.onFailure(call, e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if(log.getLevel() >= AgentLog.VERBOSE) {
            log.verbose("CallbackExtension.onResponse() - checking response.");
        }



        this.checkResponse(response);
        this.impl.onResponse(call, response);
    }

    private Response checkResponse(Response response) {
        if(!this.getTransactionState().isComplete()) {
            if(log.getLevel() >= AgentLog.VERBOSE) {
                log.verbose("CallbackExtension.checkResponse() - transaction is not complete.  Inspecting and instrumenting response.");
            }

            response = OkHttp3TransactionStateUtil.inspectAndInstrumentResponse(this.getTransactionState(), response);
        }

        return response;
    }

    private TransactionState getTransactionState() {
        return this.transactionState;
    }

    private void error(Exception e) {
        TransactionState transactionState = this.getTransactionState();
        TransactionStateUtil.setErrorCodeFromException(transactionState, e);
        if(!transactionState.isComplete()) {
            TransactionData transactionData = transactionState.end();
            if(transactionData != null) {
                TaskQueue.queue(new HttpTransactionMeasurement(transactionData.getUrl(), transactionData.getHttpMethod(), transactionData.getStatusCode(), transactionData.getErrorCode(), transactionData.getTimestamp(), (double)transactionData.getTime(), transactionData.getBytesSent(), transactionData.getBytesReceived(), transactionData.getAppData()));
            }
        }

    }
}
