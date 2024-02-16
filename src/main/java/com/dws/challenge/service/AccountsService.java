package com.dws.challenge.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.constants.DwsConstants;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;
	private final NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	// Method to create an account
	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	// Method to get account details by account ID
	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	// Method to transfer amount between accounts and send notifications
	public void transferAmount(TransferRequest transferRequest) {
		Map<String, Account> map = this.accountsRepository.transferAmount(transferRequest);

		// Send notification messages to account holders
		sendTransferNotification(map.get(DwsConstants.FROM_ACCOUNT), transferRequest, true);
		sendTransferNotification(map.get(DwsConstants.TO_ACCOUNT), transferRequest, false);
	}

	// Method to send transfer notification
	private void sendTransferNotification(Account account, TransferRequest transferRequest, boolean isFromAccount) {
		String message = isFromAccount ? createFromAccountNotificationMessage(transferRequest)
				: createToAccountNotificationMessage(transferRequest);
		notificationService.notifyAboutTransfer(account, message);
	}

	// Method to create notification message for transfer from account
	private String createFromAccountNotificationMessage(TransferRequest transferRequest) {
		return transferRequest.getFromAccountId() + " transferred " + transferRequest.getTransferAmount()
				+ " to account " + transferRequest.getToAccountId();
	}

	// Method to create notification message for transfer to account
	private String createToAccountNotificationMessage(TransferRequest transferRequest) {
		return "Received " + transferRequest.getTransferAmount() + " from account "
				+ transferRequest.getFromAccountId();
	}
}
