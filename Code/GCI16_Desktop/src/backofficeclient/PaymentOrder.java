package backofficeclient;


/**
 * Represents entity paymentOrder
 * @author GCI16_25
 */
public class PaymentOrder {
    private int id;
    private Integer protocol;
    private Status status = Status.NOTISSUED;
    private Bill bill;
    private double amount;
    
    /*Enumeration of possible payment order status.*/
    public enum Status{
        NOTISSUED, ISSUED, SUSPENDED, PAID, NOTPERTINENT, NOTIFIED;
    }
    
    public PaymentOrder(int id, Integer protocol, Status status, Bill bill,double amount){
        this(protocol, status, bill);
        this.id = id;
        this.amount = amount;
    }
    
    public PaymentOrder(Integer protocol, Status status, Bill bill){
        this.protocol = protocol;
        this.status = status;
        this.bill = bill;
        
    }
    
    public Bill getBill(){
        return bill;
    }

    public int getId(){
        return id;
    }
            
    
    public Integer getProtocol(){
        return protocol;
    }
    

    
    public Status getStatus(){
        return status;
    }
    
    public String getDebtor(){
        String name = bill.getName(); 
        String surname = bill.getSurname();
        return (name + " " +surname); 
    }
  
    public int getTrimester(){
        return bill.getTrimester();
    }
    
    public int getYear(){
        return bill.getYear();
    }
    
    public double getAmount(){
        return amount;
    }
    
    public void setStatus(Status status){
        this.status = status;
    }
    
    public void setProtocol(Integer protocol){
        this.protocol = protocol;
    }
    
    
}
