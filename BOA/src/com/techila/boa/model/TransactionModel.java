package com.techila.boa.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionModel implements Parcelable {

	private String CompanyId, LoginId, UserType, Source, Destination, Amount,
			ReqId, Reason, Pay_type, Pay_Id, Req_dt, Req_time, Status, Name,
			FBankName, TBankName;

	public TransactionModel() {
		super();
	}

	public TransactionModel(String companyId, String loginId, String uType,
			String source, String dest, String amt, String reqId, String pType,
			String reason, String pId, String date, String time, String status,
			String name, String FbankName, String DbankName) {

		this.CompanyId = companyId;
		this.LoginId = loginId;
		this.UserType = uType;
		this.Source = source;
		this.Destination = dest;
		this.Amount = amt;
		this.ReqId = reqId;
		this.Pay_type = pType;
		this.Reason = reason;
		this.Pay_Id = pId;
		this.Req_dt = date;
		this.Req_time = time;
		this.Status = status;
		this.Name = name;
		this.FBankName = FbankName;
		this.TBankName = DbankName;
	}

	public TransactionModel(Parcel in) {
		String[] data = new String[16];

		in.readStringArray(data);
		this.CompanyId = data[0];
		this.LoginId = data[1];
		this.UserType = data[2];
		this.Source = data[3];
		this.Destination = data[4];
		this.Amount = data[5];
		this.ReqId = data[6];
		this.Pay_type = data[7];
		this.Reason = data[8];
		this.Pay_Id = data[9];
		this.Req_dt = data[10];
		this.Req_time = data[11];
		this.Status = data[12];
		this.Name = data[13];
		this.FBankName = data[14];
		this.TBankName = data[15];
	}

	public String getCompanyId() {
		return CompanyId;
	}

	public void setCompanyId(String companyId) {
		CompanyId = companyId;
	}

	public String getLoginId() {
		return LoginId;
	}

	public void setLoginId(String loginId) {
		LoginId = loginId;
	}

	public String getUserType() {
		return UserType;
	}

	public void setUserType(String userType) {
		UserType = userType;
	}

	public String getSource() {
		return Source;
	}

	public void setSource(String source) {
		Source = source;
	}

	public String getDestination() {
		return Destination;
	}

	public void setDestination(String destination) {
		Destination = destination;
	}

	public String getAmount() {
		return Amount;
	}

	public void setAmount(String amount) {
		Amount = amount;
	}

	public String getReqId() {
		return ReqId;
	}

	public void setReqId(String reqId) {
		ReqId = reqId;
	}

	public String getReason() {
		return Reason;
	}

	public void setReason(String reason) {
		Reason = reason;
	}

	public String getPay_type() {
		return Pay_type;
	}

	public void setPay_type(String pay_type) {
		Pay_type = pay_type;
	}

	public String getPay_Id() {
		return Pay_Id;
	}

	public String getReq_dt() {
		return Req_dt;
	}

	public void setReq_dt(String req_dt) {
		Req_dt = req_dt;
	}

	public String getReq_time() {
		return Req_time;
	}

	public void setReq_time(String req_time) {
		Req_time = req_time;
	}

	public void setPay_Id(String pay_Id) {
		Pay_Id = pay_Id;
	}

	public String params() {

		List<NameValuePair> params1 = new ArrayList<NameValuePair>();
		params1.add(new BasicNameValuePair("companyID", getCompanyId()));
		params1.add(new BasicNameValuePair("userID", getLoginId()));
		params1.add(new BasicNameValuePair("userType", getUserType()));
		params1.add(new BasicNameValuePair("fromBankID", getSource()));
		params1.add(new BasicNameValuePair("toBankID", getDestination()));
		params1.add(new BasicNameValuePair("amount", getAmount()));
		params1.add(new BasicNameValuePair("user_request_id", getReqId()));
		params1.add(new BasicNameValuePair("paymentReason", getReason()));
		params1.add(new BasicNameValuePair("paymentType", getPay_type()));
		params1.add(new BasicNameValuePair("payment_id", getPay_Id()));

		return params1.toString();
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getFBankName() {
		return FBankName;
	}

	public void setFBankName(String fBankName) {
		FBankName = fBankName;
	}

	public String getTBankName() {
		return TBankName;
	}

	public void setTBankName(String tBankName) {
		TBankName = tBankName;
	}

	@Override
	public int describeContents() {

		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeStringArray(new String[] { this.CompanyId, this.LoginId,
				this.UserType, this.Source, this.Destination, this.Amount,
				this.ReqId, this.Pay_type, this.Reason, this.Pay_Id,
				this.Req_dt, this.Req_time, this.Status, this.Name,
				this.FBankName, this.TBankName,

		});

	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public TransactionModel createFromParcel(Parcel in) {
			// TODO Auto-generated method stub
			return new TransactionModel(in);
		}

		public TransactionModel[] newArray(int size) {
			// TODO Auto-generated method stub
			return new TransactionModel[size];
		}
	};
}
