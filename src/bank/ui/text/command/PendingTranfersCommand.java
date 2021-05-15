package bank.ui.text.command;

import java.util.List;

import bank.business.AccountManagementService;
import bank.business.domain.CurrentAccountId;
import bank.business.domain.Transfer;
import bank.business.domain.Transfer.Status;
import bank.ui.text.BankTextInterface;
import bank.ui.text.UIUtils;


public class PendingTranfersCommand extends Command {
	
	private final AccountManagementService accountManagementService;
	
	public PendingTranfersCommand (BankTextInterface bankInterface,
			AccountManagementService accountManagementService) {
		super(bankInterface);
		this.accountManagementService = accountManagementService;
	}
	
	
	public enum StatementType {
		AUTHORIZED(1), UNAUTHORISED(2);

		private int number;

		private StatementType(int number) {
			this.number = number;
		}

		public int getNumber() {
			return number;
		}
	}
	
	private StatementType readStatementType() {
		StatementType type = null;
		while (type == null) {
			for (StatementType sType : StatementType.values()) {
				System.out.println(sType.number + " - "
						+ getTextManager().getText(sType.name()));
			}
			Integer option = UIUtils.INSTANCE.readInteger("operation.type");
			for (StatementType sType : StatementType.values()) {
				if (sType.number == option) {
					type = sType;
					break;
				}
			}
		}
		return type;
	}
	
	private void printSelectedTransfer(Transfer transfer) {
		StringBuffer str = new StringBuffer();
		
		CurrentAccountId dstId;
		CurrentAccountId srcId;
		
		str.append(getTextManager().getText("source")).append("\t\t\t");
		str.append(getTextManager().getText("destination")).append("\t\t");
		str.append(getTextManager().getText("amount")).append("\n");
		str.append("--------------------------------------------------"
				+ "--------------------------------------------------\n");
		srcId = transfer.getAccount().getId();
		str.append("AG").append(srcId.getBranch().getNumber()).append(" C/C ").append(srcId.getNumber()).append("\t\t\t");
		dstId = transfer.getDestinationAccount().getId();
		str.append(" AG ").append(dstId.getBranch().getNumber()).append(" C/C ").append(dstId.getNumber()).append("\t\t\t");
		str.append(transfer.getAmount() + "\n");
		
		System.out.println(str);
	}
	
	private void printPendingTranfers(List<Transfer> pendingTransfers) {
		StringBuffer sb = new StringBuffer();
		 
		CurrentAccountId dstId;
		CurrentAccountId srcId;
		
		sb.append(getTextManager().getText("index")).append("\t\t\t");
		sb.append(getTextManager().getText("date")).append("\t\t\t");
		sb.append(getTextManager().getText("location")).append("\t\t\t");
		sb.append(getTextManager().getText("source")).append("\t\t\t");
		sb.append(getTextManager().getText("destination")).append("\t\t");
		sb.append(getTextManager().getText("amount")).append("\n");
		sb.append("--------------------------------------------------"
				+ "--------------------------------------------------"
				+ "------------------------------------------------------\n");
		for (Transfer transfer : pendingTransfers) {
			
			sb.append(pendingTransfers.indexOf(transfer) + "\t\t");
			sb.append(UIUtils.INSTANCE.formatDateTime(transfer.getDate())).append("\t\t\t");
			sb.append(transfer.getLocation()).append("\t\t\t");
			srcId = transfer.getAccount().getId();
			sb.append("AG").append(srcId.getBranch().getNumber()).append(" C/C ").append(srcId.getNumber()).append("\t\t\t");
			dstId = transfer.getDestinationAccount().getId();
			sb.append(" AG ").append(dstId.getBranch().getNumber()).append(" C/C ").append(dstId.getNumber()).append("\t\t\t");
			sb.append(transfer.getAmount() + "\n");
		}
		System.out.println(sb);
	}
	
	private Integer chooseIndex(List<Transfer> pendingTransfers) {
		System.out.println();
		Integer indexAccount;
		
		do {
			indexAccount = UIUtils.INSTANCE.readInteger("index.account");
		} while (indexAccount < 0 || indexAccount > pendingTransfers.size() - 1);
		
		return indexAccount;
	}
	
	@Override
	public void execute() throws Exception {
	
		List<Transfer> pendingTransfers = this.accountManagementService.getPendingTranfers();
		Integer index;
		
		printPendingTranfers(pendingTransfers);
		
		if (!pendingTransfers.isEmpty()) {
			index = chooseIndex(pendingTransfers);
			
			Transfer transfer = pendingTransfers.get(index);
			
			printSelectedTransfer(transfer);
			
			StatementType statementType = readStatementType();
			
			switch (statementType) {
				case AUTHORIZED:
					this.accountManagementService.updateTranferStatusTo(Status.FINISHED, transfer);
					break;
				case UNAUTHORISED:
					this.accountManagementService.updateTranferStatusTo(Status.CANCELED, transfer);
					break;
			}
		}
	}
}
