package bank.business.domain;

/**
 * @author Ingrid Nunes
 * 
 */
public class Transfer extends Transaction {
	public enum Status {
		FINISHED, PENDING, CANCELED
	};

	public static final int MAX_AUTOAUTH_AMOUNT = 5000;
	
	private CurrentAccount destinationAccount;
	private Status status; 

	public Transfer(OperationLocation location, CurrentAccount account,
			CurrentAccount destinationAccount, double amount) {
		super(location, account, amount);
		this.destinationAccount = destinationAccount;
		if (amount < MAX_AUTOAUTH_AMOUNT || location instanceof Branch) {
			this.status = Status.FINISHED;
		} else {
			this.status = Status.PENDING;
		}
	}

	/**
	 * @return the destinationAccount
	 */
	public CurrentAccount getDestinationAccount() {
		return destinationAccount;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public boolean isPending() {
		return this.getStatus() == Status.PENDING;
	}
	
	public boolean isFinished() {
		return this.getStatus() == Status.FINISHED;
	}
}
