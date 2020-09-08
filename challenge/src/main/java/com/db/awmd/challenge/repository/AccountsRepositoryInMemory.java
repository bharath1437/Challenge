package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.NotificationService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();
	
	private final NotificationService notificationService = new EmailNotificationService();
	 
	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}
	
	@Override
	public synchronized String transferAmount(String fromAccountId, String toAccountId, int amount) {
		String msg = "Amount transfered succesfully";	
		if((accounts.get(fromAccountId)!=null && accounts.get(toAccountId)!=null)){
				if(amount>0 && accounts.get(fromAccountId).getBalance().intValue()>=amount) {
					getAccount(toAccountId).setBalance(getAccount(toAccountId).getBalance().add(new BigDecimal(amount)));
					 accounts.get(fromAccountId).setBalance(accounts.get(fromAccountId).getBalance().subtract(new BigDecimal(amount)));
					 notificationService.notifyAboutTransfer(accounts.get(fromAccountId),"rs "+amount+" tranfered to AccountID "+toAccountId);
					 notificationService.notifyAboutTransfer(accounts.get(toAccountId),"rs "+amount+"credited from AccountID "+fromAccountId);
				}else {
					msg = "amount Should not be negative or Amount should be less than available balance";
				}
			}
			else {
				msg = "fromaccountId and toAccountId Should Exsists plz enter valid account Numbers";
			}
				
			return msg;
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

}
