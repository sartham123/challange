package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	// successfully transfered amount between accounts
	@Test
	void trasfer() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-567\",\"transferAmount\":200}"))
				.andExpect(status().isOk());

		Account fromAccount = accountsService.getAccount("Id-123");
		assertThat(fromAccount.getBalance()).isEqualByComparingTo("800");
		Account toAccount = accountsService.getAccount("Id-567");
		assertThat(toAccount.getBalance()).isEqualByComparingTo("2200");
	}

	// should not transfer between same from and to account Id case
	@Test
	void trasferFromAndToAccountSameId() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-123\",\"transferAmount\":200}"))
				.andExpect(status().isBadRequest());
	}

	// BadRequest when from account is less than transfer amount
	@Test
	void trasferFromAccountAmountLessThantTransferAmount() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-567\",\"transferAmount\":10000}"))
				.andExpect(status().isBadRequest());
	}

	// BadRequest when from account is empty
	@Test
	void trasferEmptyFromAccount() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"\",\"toAccountId\":\"Id-567\",\"transferAmount\":10000}"))
				.andExpect(status().isBadRequest());
	}

	// BadRequest when to account is empty
	@Test
	void trasferEmptyToAccount() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"\",\"transferAmount\":10000}"))
				.andExpect(status().isBadRequest());
	}

	// BadRequest when transferring negative amount
	@Test
	void trasferNegativeAmount() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"\",\"transferAmount\":-123}"))
				.andExpect(status().isBadRequest());
	}

	// BadRequest when transferring 0 amount
	@Test
	void trasferZeroAmount() throws Exception {
		// creating accounts to transfer amount between accounts
		createAccountsData();
		this.mockMvc
				.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"\",\"transferAmount\":0}"))
				.andExpect(status().isBadRequest());
	}

	// creating Accounts for transfer tests
	private void createAccountsData() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}"));
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-567\",\"balance\":2000}"));

	}

}
