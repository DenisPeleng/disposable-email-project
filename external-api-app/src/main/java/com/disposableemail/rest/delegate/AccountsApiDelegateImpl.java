package com.disposableemail.rest.delegate;


import com.disposableemail.api.AccountsApiDelegate;
import com.disposableemail.core.dao.mapper.AccountMapper;
import com.disposableemail.core.exception.custom.AccountNotFoundException;
import com.disposableemail.core.model.Account;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountsApiDelegateImpl implements AccountsApiDelegate {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Account>> deleteAccountItem(String id, ServerWebExchange exchange) {

        return accountService.softDeleteAccount(id)
                .map(accountMapper::accountEntityToAccount)
                .map(account -> ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(account));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Account>> getAccountItem(String id, ServerWebExchange exchange) {

        return accountService.getAccountById(id)
                .map(accountEntity -> {
                    log.info("Retrieved Account: {}", accountEntity.getAddress());
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(accountMapper.accountEntityToAccount(accountEntity));
                })
                .switchIfEmpty(Mono.error(new AccountNotFoundException()));
    }

    @Override
    public Mono<ResponseEntity<Account>> createAccountItem(Mono<Credentials> credentials, ServerWebExchange exchange) {

        return credentials.flatMap(accountService::createAccount)
                .map(accountEntity -> {
                    log.info("Saved Account: {}", accountEntity.getAddress());
                    return ResponseEntity.status(HttpStatus.ACCEPTED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(accountMapper.accountEntityToAccount(accountEntity));
                });
    }
}
