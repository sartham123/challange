package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.TransferException;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	void addAccount() {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	void addAccount_failsOnDuplicateId() {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}

	// Successfully transfered amount between accounts
	@Test
	void transferAmount_Success() {
		// Mock accounts for transfer
		Account fromAccount = new Account("1");
		fromAccount.setBalance(BigDecimal.valueOf(100));
		Account toAccount = new Account("2");
		toAccount.setBalance(BigDecimal.valueOf(50));
		this.accountsService.createAccount(fromAccount);
		this.accountsService.createAccount(toAccount);

		// Mock transfer request
		TransferRequest transferRequest = new TransferRequest("1", "2", BigDecimal.TEN);

		this.accountsService.transferAmount(transferRequest);
		assertThat(this.accountsService.getAccount(fromAccount.getAccountId()).getBalance())
				.isEqualTo(BigDecimal.valueOf(90));
		assertThat(this.accountsService.getAccount(toAccount.getAccountId()).getBalance())
				.isEqualTo(BigDecimal.valueOf(60));
	}

	// Error message when from Account Id and to Account Id same generally transfer
	// should not happen on same account .
	@Test
	void transferAmount_failsOnFromAccAndToAccSame() {
		// Mock accounts for transfer
		Account fromAccount = new Account("3");
		fromAccount.setBalance(BigDecimal.valueOf(100));
		Account toAccount = new Account("5");
		toAccount.setBalance(BigDecimal.valueOf(50));
		this.accountsService.createAccount(fromAccount);
		this.accountsService.createAccount(toAccount);

		// Mock transfer request
		TransferRequest transferRequest = new TransferRequest("5", "5", BigDecimal.TEN);

		try {
			this.accountsService.transferAmount(transferRequest);
			fail("Should have failed when amount transfer between same from and to accountId");
		} catch (TransferException ex) {
			assertThat(ex.getMessage()).isEqualTo("From and To Account Ids should not be same");
		}

	}

	// Error message when from Account balance is less than transfer amount.
	@Test
	void transferAmount_failsOnIfFromAccBalLessThanTransferAmount() {
		// Mock accounts for transfer
		Account fromAccount = new Account("222");
		fromAccount.setBalance(BigDecimal.valueOf(100));
		Account toAccount = new Account("555");
		toAccount.setBalance(BigDecimal.valueOf(50));
		this.accountsService.createAccount(fromAccount);
		this.accountsService.createAccount(toAccount);

		// Mock transfer request
		TransferRequest transferRequest = new TransferRequest("222", "555", BigDecimal.valueOf(1000));

		try {
			this.accountsService.transferAmount(transferRequest);
			fail("Should have failed when transfer amount is greater than from account amount");
		} catch (TransferException ex) {
			assertThat(ex.getMessage()).isEqualTo("Insufficient funds");
		}

	}

	// Error message when non exist account in transfer
	@Test
	void transferAmount_failsOnIfNonExistAccountInTransferAmount() {
		// Mock accounts for transfer
		Account fromAccount = new Account("2221");
		fromAccount.setBalance(BigDecimal.valueOf(100));
		Account toAccount = new Account("5552");
		toAccount.setBalance(BigDecimal.valueOf(50));
		this.accountsService.createAccount(fromAccount);
		this.accountsService.createAccount(toAccount);

		// Mock transfer request
		TransferRequest transferRequest = new TransferRequest("22267", "555", BigDecimal.valueOf(1000));

		try {
			this.accountsService.transferAmount(transferRequest);
			fail("Should have failed when non exist exist account id in transfer");
		} catch (TransferException ex) {
			assertThat(ex.getMessage()).isEqualTo("Invalid 22267 Account id");
		}

	}
}
