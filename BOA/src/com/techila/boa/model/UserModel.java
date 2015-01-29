package com.techila.boa.model;

public class UserModel {

	private String FirstName, LastName, Age, Sex, Email, Postal, Type;

	public UserModel(String fname, String lname, String age, String sex,
			String email, String postal_add, String type) {

		this.FirstName = fname;
		this.LastName = lname;
		this.Age = age;
		this.Sex = sex;
		this.Email = email;
		this.Postal = postal_add;
		this.Type = type;
	}

	public String getFirstName() {
		return FirstName;
	}

	public void setFirstName(String firstName) {
		FirstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}

	public void setLastName(String lastName) {
		LastName = lastName;
	}

	public String getAge() {
		return Age;
	}

	public void setAge(String age) {
		Age = age;
	}

	public String getSex() {
		return Sex;
	}

	public void setSex(String sex) {
		Sex = sex;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getPostal() {
		return Postal;
	}

	public void setPostal(String postal) {
		Postal = postal;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}
	

}
