package com.example.Bank.App.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.Bank.App.entity.account;


@Repository
public interface accountRepository extends JpaRepository<account,Long>{
    account findByNumber(Long number);
    boolean existsByNumber(Long number);
    @Query(value = "SELECT * FROM accounts where number = 'number'", nativeQuery = true)
    List<account> search(Long number);
    List<account> findAllByNumber(Long accountNumber);
}

