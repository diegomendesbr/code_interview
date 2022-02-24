//MATH CONFIGURATION
class Rounding {
    public BigDecimal round(int n) {
        return setScale(n, BigDecimal.ROUND_HALF_UP);
    }
}

BigDecimal.mixin Rounding

//DOMAIN
//List of commands received from the file
commands = [:]
//List of applicants filtered from the addition command
applications = [:]

class Loan{
	String applicantName, id
	double loanAmount, annualInterestRate, monthlyPayment, monthlyInterestRate
	int terms 
	
	Loan(applicantName, id, loanAmount, annualInterestRate, terms){
		this.id = id
		this.applicantName = applicantName
		this.loanAmount = loanAmount
		this.annualInterestRate = annualInterestRate
		this.terms = terms
		//monthly interest rate = annual interest rate / terms
		this.monthlyInterestRate = ( annualInterestRate.round(10) / terms) / 100
	}

	def greet() { 
		println "${applicantName}:${id}" 
	}
	
	def setMonthlyPayment(){

		//monthlyPayment = (monthly interest rate * loanAmount * (1 + monthly interest rate)power number of Months) 
		//                                       / (1 + monthly interest rate)power number of Months) - 1
		
		def up = ( monthlyInterestRate.round(10) * loanAmount.round(10) * (1 + monthlyInterestRate.round(10)).power(terms))
		def down = ((1 + monthlyInterestRate.round(10) ).power(terms) - 1)

		monthlyPayment = (up/down).round(10)

	}
	
	def amortizationSchedule(){
		double interestPaid, loanAmountPaid, newBalance
		
		setMonthlyPayment()
		printTableHeader()
		
		try{
			for (int month = 1; month <= terms; month++) {
				interestPaid = loanAmount * (monthlyInterestRate)
			
				loanAmountPaid = monthlyPayment - interestPaid
			
				newBalance = loanAmount - loanAmountPaid
			
				printScheduleLoan(month, interestPaid, loanAmountPaid, newBalance)
			
				// Update the balance
				loanAmount = newBalance
			}
			
		}catch(Exception e){
			println e
		}
		
	
	}
	
	def printScheduleLoan(int month, double interestPaid, double loanAmountPaid, double newBalance) {
		println "\$${loanAmountPaid.round(2)} \$${interestPaid.round(2)} \$${newBalance.round(2)}"

	}


	def printTableHeader() {
		//Principal Interest Balance 
		println "Principal Interest Balance"
	}
}

def startLoans(){
	commands.sort().each{ entry -> 
		//Only applications with Add command will be created
		if(entry.value[0].equals("Add")){								
			def loan = new Loan(entry.value[1], entry.value[2], entry.value[3].replace('$',' ').trim() as double, 
										entry.value[4].replace('%',' ').trim()as double, entry.value[5] as int) 									
			applications[loan.applicantName]=loan
		}
	}
}

def loadFile(args){
	// line example - Add Tom 101-1313-101 $300 35.5% 12
	// line pattern - COMMAND NAME ID LOAN INTEREST_RATE TERMS
	new File(args[0]).eachLine { line ->
		def command = line.split()
	    commands["${command[1]}"] = command
	}
}

def printAmortizationSchedules(){
	applications.each{ application ->
		printAmortizationSchedule(application.value)
	}
}

def printAmortizationSchedule(Loan loan){
	loan.greet()
	loan.amortizationSchedule()
	println ""
}


//WORKFLOW	
loadFile(args)
startLoans()
printAmortizationSchedules()

