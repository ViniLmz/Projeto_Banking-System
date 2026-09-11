package com.bank_project.account;

import com.bank_project.custumer.Custumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


import java.math.BigDecimal;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String accountNumber;

    private BigDecimal balance;

    private String accountType;

    private String status;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id")

    private Custumer customer;

    public void setCustomer(Custumer customer) {
        this.customer = customer;
    }
    public Custumer getCustomer (){
        return  customer;
    }

public  Account  (Long id, String accountNumber, BigDecimal balance,String accountType,String status ){

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
