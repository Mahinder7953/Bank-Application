package com.example.Bank.App.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.Bank.App.entity.account;
import com.example.Bank.App.service.accountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class accountController {
    @Autowired
    private accountService accountServ;

    @GetMapping("/")
    public String getAccounts(Model model,@RequestParam(defaultValue = "") Long accountNumber) {
        List<account> acs = accountServ.getAccounts(accountNumber);
        model.addAttribute("accounts", acs);
        return "index";
    }

    //CREATE ACCOUNT PAGE
    @GetMapping("/add")
    public String createAccountPage() {
        return "createAccount";
    }
    //ADD A/C
    @PostMapping("/add")
    public String addAccount(@ModelAttribute("account") account ac) {
        accountServ.addAccount(ac);
        return "redirect:/";
    }
    
    //DEPOSIT PAGE
    @GetMapping("/deposit/{number}")
    public String depositPage(@PathVariable Long number,Model model) {
        model.addAttribute("accountDetail",accountServ.accountDetail(number));
        return "depositPage";
    }
    

    //DEPOSIT
    @PostMapping("/deposit/{number}")
    public String deposit(@PathVariable Long number,@RequestParam Long amount) {
        accountServ.deposit(amount,number);
        return "redirect:/";
    }

    //WITHDRAW PAGE
    @GetMapping("/withdraw/{number}")
    public String withdrawPage(@PathVariable Long number,Model model) {
        model.addAttribute("accountDetail",accountServ.accountDetail(number));
        return "withdrawPage";
    }
    

    //DEDUCT MONEY
    @PostMapping("/withdraw/{number}")
    public String withdraw(@RequestParam Long amount,@PathVariable Long number) {
        accountServ.withdraw(amount,number);
        return "redirect:/";
    }

    //TRANSFER PAGE
    @GetMapping("/transfer/{number}")
    public String transferPage(@PathVariable Long number,Model model) {
        model.addAttribute("accountDetail",accountServ.accountDetail(number));
        return "transferPage";
    }
    

    //TRANSFER MONEY
    @PostMapping("/transfer/{number}")
    public String transfer(@RequestParam Long receiver,@RequestParam Long amount,@PathVariable Long number) {
        accountServ.transfer(receiver,amount,number);
        return "redirect:/";
    }

    //UPDATE PAGE
    @GetMapping("/update/{number}")
    public String updatePage(@PathVariable Long number,Model model) {
        model.addAttribute("accountDetail",accountServ.accountDetail(number));
        return "updatePage";
    }
    

    //UPDATE DETAILS
    @PostMapping("/update/{number}")
    public String updateAccount(@PathVariable Long number,@ModelAttribute("account") account updates) {
        accountServ.updateAccount(number,updates);
        return "redirect:/";
    }

    //DELETE ACCOUNT
    @GetMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountServ.deleteAccount(id);
        return "redirect:/";
    }

}