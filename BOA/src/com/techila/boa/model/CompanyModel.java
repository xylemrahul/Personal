package com.techila.boa.model;

public class CompanyModel {

	private String C_Name,Tan,Pan,C_Add;
	
	public CompanyModel(String comName,String tan,String pan,String address) {

		this.C_Name = comName;
		this.Tan = tan;
		this.Pan = pan;
		this.C_Add = address;
	}
	
	public String getCompany(){
		return C_Name;
	}
	
	public void setCompany(String company){
		this.C_Name = company;
	}
	
	public String getTan(){
		return Tan;
	}
	
	public void setTan(String tan){
		this.Tan = tan;
	}
	
	public String getPan(){
		return Pan;
	}
	
	public void setPan(String pan){
		this.Pan = pan;
	}
	
	public String getAddress(){
		return C_Add;
	}
	
	public void setAddress(String add){
		this.C_Add = add;
	}
}
