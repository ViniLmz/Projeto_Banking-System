package com.bank_project.custumerModel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class Custumer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty(message = "Please, enter the full name")
    private String name;
    private String email;
    private String cpf;


    public Custumer (Long id, String name, String cpf, String email) {
        this.id = id;
        this.name = name;
        this.cpf=cpf;
        this.email=email;
    }
    public Custumer () {}


    public void setId(Long id) {
        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public void  setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCpf (String cpf) {
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }

}
