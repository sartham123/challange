package com.dws.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.dws.challenge.constants.DwsConstants;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.TransferException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	// Map to store accounts in memory
	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	// Method to create a new account
	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	// Method to get account details by accountId
	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	// Method to clear all accounts
	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	// Method to transfer amount between accounts
	@Override
	public Map<String, Account> transferAmount(TransferRequest transferRequest) {

		// Get 'from' and 'to' accounts based on account ids from transfer request
		Account fromAccount = accounts.get(transferRequest.getFromAccountId());
		Account toAccount = accounts.get(transferRequest.getToAccountId());

		// Check if 'from' account exists
		if (fromAccount == null) {
			throw new TransferException("Invalid " + transferRequest.getFromAccountId() + " Account id");
		}

		// Check if 'to' account exists
		if (toAccount == null) {
			throw new TransferException("Invalid " + transferRequest.getToAccountId() + " Account id");
		}

		// Check if to and from account should not be same
		if (transferRequest.getFromAccountId().equals(transferRequest.getToAccountId())) {
			throw new TransferException("From and To Account Ids should not be same");
		}

		// Acquire locks 'from' and 'to' accounts to prevent race condition while
		// updating balances
		try {
			fromAccount.lock();
			try {
				toAccount.lock();
				// Check if 'from' account has sufficient balance for transfer
				if (fromAccount.getBalance().compareTo(transferRequest.getTransferAmount()) < 0) {
					throw new TransferException("Insufficient funds");
				}

				// Update balances for 'from' and 'to' accounts
				// Getting latest data if some changes before acquiring lock
				// Here can implement Version number but not required in this case
				fromAccount = accounts.get(transferRequest.getFromAccountId());
				toAccount = accounts.get(transferRequest.getToAccountId());
				fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getTransferAmount()));
				toAccount.setBalance(toAccount.getBalance().add(transferRequest.getTransferAmount()));
			} finally {
				toAccount.releaseLock();
			}
		} finally {
			fromAccount.releaseLock();
		}

		// Return an immutable map containing the updated 'from' and 'to' accounts
		return Map.ofEntries(Map.entry(DwsConstants.FROM_ACCOUNT, fromAccount),
				Map.entry(DwsConstants.TO_ACCOUNT, toAccount));
	}
}
