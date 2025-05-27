package com.example.Bank.App.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Bank.App.entity.account;
import com.example.Bank.App.repository.accountRepository;

@Service
public class accountService {
    @Autowired
    private accountRepository accountRepo;

    public List<account> getAccounts(Long accountNumber) {
        if (accountNumber==null) {
            return accountRepo.findAll();
        }
        List<account> acs = accountRepo.findAllByNumber(accountNumber);
        return  acs;
    }

    public void addAccount(account ac) {
        accountRepo.save(ac);
    }

    public account accountDetail(Long number) {
        return accountRepo.findByNumber(number);
    }

    public void deposit(Long amount, Long number) {
        account Ac = accountRepo.findByNumber(number);
        if (amount>0) {
            Ac.setBalance((Ac.getBalance()+amount));
            accountRepo.save(Ac);
        }
    }

    public void withdraw(Long amount, Long number) {
        account Ac = accountRepo.findByNumber(number);
        if (Ac.getBalance()>0 && amount>0 && Ac.getBalance()>=amount) {
            
            Ac.setBalance((Ac.getBalance()-amount));
            accountRepo.save(Ac);
        }
    }

    public void transfer(Long receiver, Long amount,Long number) {
        account sender = accountRepo.findByNumber(number);
        if (accountRepo.existsByNumber(receiver) && amount>0 && sender.getBalance()>amount) {
            deposit(amount, receiver);
            withdraw(amount, number);
            System.out.println("done");
        }
        else{
            System.out.println("ERROR");
            return;
        }
    }

    public void updateAccount(Long number, account updates) {
        account ac = accountRepo.findByNumber(number);

        ac.setName(updates.getName());
        ac.setPin(updates.getPin());
        ac.setBalance(updates.getBalance());

        accountRepo.save(ac);
    }

    public void deleteAccount(Long id) {
        accountRepo.deleteById(id);
    }

}
