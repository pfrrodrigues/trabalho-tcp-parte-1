package bank.business.domain;

import java.util.ArrayList;
import java.util.List;

import bank.business.BusinessException;
import bank.business.domain.Transfer.Status;
import bank.ui.text.UIUtils;

/**
 * @author Ingrid Nunes
 * 
 */
public class CurrentAccount implements Credentials {

	private double balance;
	private Client client;
	private List<Deposit> deposits;
	private CurrentAccountId id;
	private List<Transfer> transfers;
	private List<Transfer> pendingTransfers;
	private List<Withdrawal> withdrawals;
	
	public static final int MAX_AUTOAUTH_AMOUNT = 5000;

	public CurrentAccount(Branch branch, long number, Client client) {
		this.id = new CurrentAccountId(branch, number);
		branch.addAccount(this);
		this.client = client;
		client.setAccount(this);
		this.deposits = new ArrayList<>();
		this.transfers = new ArrayList<>();
		this.pendingTransfers = new ArrayList<>();
		this.withdrawals = new ArrayList<>();
	}
	
	public CurrentAccount(Branch branch, long number, Client client,
			double initialBalance) {
		this(branch, number, client);
		this.balance = initialBalance;
	}

	public Deposit deposit(OperationLocation location, long envelope,
			double amount) throws BusinessException {
		depositAmount(amount);

		Deposit deposit = new Deposit(location, this, envelope, amount);
		this.deposits.add(deposit);

		return deposit;
	}

	private void depositAmount(double amount) throws BusinessException {
		if (!isValidAmount(amount)) {
			throw new BusinessException("exception.invalid.amount");
		}

		this.balance += amount;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @return the deposits
	 */
	public List<Deposit> getDeposits() {
		return deposits;
	}

	/**
	 * @return the id
	 */
	public CurrentAccountId getId() {
		return id;
	}

	public List<Transaction> getTransactions() {
		List<Transaction> transactions = new ArrayList<>(deposits.size()
				+ withdrawals.size() + transfers.size());
		transactions.addAll(deposits);
		transactions.addAll(withdrawals);
		transactions.addAll(transfers);
		transactions.addAll(pendingTransfers);
		return transactions;
	}

	/**
	 * @return the transfers
	 */
	public List<Transfer> getTransfers() {
		return transfers;
	}

	/**
	 * @return the withdrawals
	 */
	public List<Withdrawal> getWithdrawals() {
		return withdrawals;
	}

	private boolean hasEnoughBalance(double amount) {
		return amount <= balance;
	}

	private boolean isValidAmount(double amount) {
		return amount > 0;
	}

	public Transfer transfer(OperationLocation location,
			CurrentAccount destinationAccount, double amount)
			throws BusinessException {
		withdrawalAmount(amount);
		Transfer.Status transferStatus = defineTransferStatus(amount, location); 
		
		Transfer transfer = new Transfer(location, this, destinationAccount,
				amount, transferStatus);
				
		if (transfer.isPending()) {			
			this.pendingTransfers.add(transfer);
		} else {
			this.transfers.add(transfer);
			destinationAccount.depositAmount(amount);
			destinationAccount.transfers.add(transfer);
		}

		return transfer;
	}

	public Withdrawal withdrawal(OperationLocation location, double amount)
			throws BusinessException {
		withdrawalAmount(amount);

		Withdrawal withdrawal = new Withdrawal(location, this, amount);
		this.withdrawals.add(withdrawal);

		return withdrawal;
	}

	private void withdrawalAmount(double amount) throws BusinessException {
		if (!isValidAmount(amount)) {
			throw new BusinessException("exception.invalid.amount");
		}

		if (!hasEnoughBalance(amount)) {
			throw new BusinessException("exception.insufficient.balance");
		}

		this.balance -= amount;
	}
	
	private void removeFromPendingTransfers(Transfer transfer) {
		this.pendingTransfers.remove(transfer);
	}
	
	private boolean transferWasAddedInTransferList(Transfer transfer) {
		return this.transfers.add(transfer);
	}
	
	public void finishTransfer(Transfer transfer) {
		transfer.setStatus(Status.FINISHED);
		transfer.getDestinationAccount().receiveTransfer(transfer);
		
		if (transferWasAddedInTransferList(transfer)) {
			removeFromPendingTransfers(transfer);
		}
	}
	
	public void cancelTransfer(Transfer transfer) {
		transfer.setStatus(Status.CANCELED);
		chargebackTransfer(transfer);
		if (transferWasAddedInTransferList(transfer)) {
			removeFromPendingTransfers(transfer);
		}
	}
	
	private void receiveTransfer(Transfer transfer) {
		this.transfers.add(transfer);
		this.balance += transfer.getAmount();
	}
	
	private void chargebackTransfer(Transfer transfer) {
		try {
			this.depositAmount(transfer.getAmount());
		} catch (BusinessException e) {
			e.printStackTrace();
		}
	}

	private Transfer.Status defineTransferStatus(double amount, OperationLocation location) {
		Transfer.Status transferStatus;
		if (amount < MAX_AUTOAUTH_AMOUNT || location instanceof Branch) {
			transferStatus = Status.FINISHED;
		} else {
			transferStatus = Status.PENDING;
		}
		return transferStatus;
	}
}
