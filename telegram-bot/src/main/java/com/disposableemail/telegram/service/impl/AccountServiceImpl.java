package com.disposableemail.telegram.service.impl;

import com.disposableemail.telegram.bot.event.AccountDeletionEventPublisher;
import com.disposableemail.telegram.client.disposableemail.webclient.model.Credentials;
import com.disposableemail.telegram.dao.entity.AccountEntity;
import com.disposableemail.telegram.dao.repository.AccountRepository;
import com.disposableemail.telegram.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountDeletionEventPublisher publisher;
    private final TextEncryptor encryptor;

    @Override
    public Optional<AccountEntity> findByAddress(String address) {
        log.info("Getting an Account by address | {}", address);
        return accountRepository.findByAddress(address);
    }

    @Override
    public void deleteByAddress(String address) {
        log.info("Deleting an Account by address | {}", address);
        var account = accountRepository.findByAddress(address);
        account.ifPresent(accountEntity -> {
            var credentials = new Credentials();
            credentials.setAddress(accountEntity.getAddress());
            credentials.setPassword(encryptor.decrypt(accountEntity.getPassword()));
            accountRepository.deleteById(accountEntity.getId());
            publisher.publish(credentials);
        });
    }

    @Override
    public AccountEntity createAccount(AccountEntity accountEntity) {
        log.info("Creating an Account | {}", accountEntity.getAddress());
        return accountRepository.save(accountEntity);
    }
}
