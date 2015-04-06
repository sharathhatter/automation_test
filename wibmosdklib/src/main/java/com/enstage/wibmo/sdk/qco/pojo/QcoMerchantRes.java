package com.enstage.wibmo.sdk.qco.pojo;

import java.io.Serializable;

public class QcoMerchantRes implements Serializable {
	private static final long serialVersionUID = 1L;

	private String dataReqStatus;
	private String dataUserStatusCode;
	private String dataReqStatusDesc;
	
	private String dataShareTxnID;
	private String dataPickUpCode;
	
	public String getDataReqStatus() {
		return dataReqStatus;
	}
	public void setDataReqStatus(String dataReqStatus) {
		this.dataReqStatus = dataReqStatus;
	}
	public String getDataUserStatusCode() {
		return dataUserStatusCode;
	}
	public void setDataUserStatusCode(String dataUserStatusCode) {
		this.dataUserStatusCode = dataUserStatusCode;
	}
	public String getDataReqStatusDesc() {
		return dataReqStatusDesc;
	}
	public void setDataReqStatusDesc(String dataReqStatusDesc) {
		this.dataReqStatusDesc = dataReqStatusDesc;
	}
	public String getDataShareTxnID() {
		return dataShareTxnID;
	}
	public void setDataShareTxnID(String dataShareTxnID) {
		this.dataShareTxnID = dataShareTxnID;
	}
	public String getDataPickUpCode() {
		return dataPickUpCode;
	}
	public void setDataPickUpCode(String dataPickUpCode) {
		this.dataPickUpCode = dataPickUpCode;
	}
	
}
