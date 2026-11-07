package com.bank_project.account.http;

import com.bank_project.account.domain.AccountStatus;

public record StatusRequest(AccountStatus status) {
}