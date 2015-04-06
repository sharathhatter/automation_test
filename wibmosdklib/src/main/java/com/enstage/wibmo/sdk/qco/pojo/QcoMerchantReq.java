package com.enstage.wibmo.sdk.qco.pojo;

import java.io.Serializable;

public class QcoMerchantReq implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String registerEventLink;
	private String eventStatusPollingLink;
	private String transactionLink;
	
	public String getRegisterEventLink() {
		return registerEventLink;
	}
	public void setRegisterEventLink(String registerEventLink) {
		this.registerEventLink = registerEventLink;
	}
	public String getEventStatusPollingLink() {
		return eventStatusPollingLink;
	}
	public void setEventStatusPollingLink(String eventStatusPollingLink) {
		this.eventStatusPollingLink = eventStatusPollingLink;
	}
	public String getTransactionLink() {
		return transactionLink;
	}
	public void setTransactionLink(String transactionLink) {
		this.transactionLink = transactionLink;
	}	
	
}
