package backofficeclient.entities;

/**
 * Represents entity bill.
 * @author GCI16_25
 */
public class Bill {
    private int id;
    private Customer customer;
    private double cost;
    private int year;
    private int trimester;
    
    /**
     * Constructor.
     * @param id id of bill.
     * @param customer person who has to pay the bill.
     * @param cost amount of bill.
     * @param year base year of bill.
     * @param trimester base trimester of bill.
     */
    public Bill(int id, Customer customer,double cost,int year,int trimester){
        this.id = id;
        this.customer = customer;
        this.cost = cost;
        this.year = year;
        this.trimester = trimester;
    }
    
    public int getId(){
        return id;
    }
    
    public Customer getCustomer(){
        return customer;
    }
    
    public int getTrimester(){
        return trimester;
    }
    
    public int getYear(){
        return year;
    }
    
    public double getCost(){
        return cost;
    }
    
    public String getName(){
        return customer.getName();
    }

    public String getSurname(){
        return customer.getSurname();
    }
    
}