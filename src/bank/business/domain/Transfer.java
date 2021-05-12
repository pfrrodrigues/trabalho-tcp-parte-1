package bank.business.domain;

/**
 * @author Ingrid Nunes
 * 
 */
public class Transfer extends Transaction {
	public enum Status {
		FINISHED, PENDING, CANCELED
	};
	
	private CurrentAccount destinationAccount;
	private Status status; 

	public Transfer(OperationLocation location, CurrentAccount account,
			CurrentAccount destinationAccount, double amount, Status status) {
		super(location, account, amount);
		this.destinationAccount = destinationAccount;
		this.status = status;
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
