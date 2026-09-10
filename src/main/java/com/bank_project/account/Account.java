package com.bank_project.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;

import java.math.BigDecimal;


@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String accountNumber;

    private BigDecimal balance;

    private String accountType;

    private String status;


public  Account  (long id, String accountNumber, BigDecimal balance,String accountType,String status ){

    this.id=id;
    this.accountNumber = accountNumber;
    this.balance = balance;
    this.accountType =accountType;
    this.status = status;
}

public  Account (){}

public void setId (Long id){
    this.id =id;
}

public Long  getId() {
    return id;
}

public void setAccountNumber (String accountNumber) {

    this.accountNumber = accountNumber;
}

public String getAccountNumber(){

return accountNumber;

}

public void setBalance (BigDecimal balance){

    this.balance = balance;
}

public BigDecimal getBalance (){
    return  balance;

}

public void setAccountType (String accountType){
    this.accountType = accountType;
}

public String getAccountType (){
    return  accountType;

}

public void setStatus(String status){
    this.status = status;

}

public String getStatus (){

    return  status;
}


}
