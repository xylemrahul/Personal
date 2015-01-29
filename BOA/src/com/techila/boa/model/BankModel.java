package com.techila.boa.model;

public class BankModel {

	private String BeneficiaryName, Account_No, AccountType, BankName, IFSC,
			InitialBalance;

	public BankModel(String ben_name, String b_act, String b_type,
			String b_name, String b_ifsc, String b_init) {

		this.BeneficiaryName = ben_name;
		this.Account_No = b_act;
		this.AccountType = b_type;
		this.BankName = b_name;
		this.IFSC = b_ifsc;
		this.InitialBalance = b_init;
	}
	
	public String getBenName() {
		return BeneficiaryName;
	}

	public void setBenname(String b_name) {
		this.BeneficiaryName = b_name;
	}
	
	public String getBankName() {
		return BankName;
	}

	public void setBankName(String bank_name) {
		this.BankName = bank_name;
	}
	
	public String getActType() {
		return AccountType;
	}

	public void setActType(String act_type) {
		this.AccountType = act_type;
	}
	
	public String getInitialBlnc() {
		return InitialBalance;
	}

	public void setInitialBalance(String i_blnc) {
		this.InitialBalance = i_blnc;
	}
	
	public String getifsc() {
		return IFSC;
	}

	public void setifsc(String b_ifsc) {
		this.IFSC = b_ifsc;
	}
	
	public String getAct_no() {
		return Account_No;
	}

	public void setAct_no(String b_no) {
		this.Account_No = b_no;
	}
}
