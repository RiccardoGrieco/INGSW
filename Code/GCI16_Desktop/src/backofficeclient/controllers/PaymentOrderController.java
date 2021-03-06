package backofficeclient.controllers;

import backofficeclient.entities.Bill;
import backofficeclient.views.BillForm;
import backofficeclient.views.ConfirmPanel;
import backofficeclient.entities.PaymentOrder;
import backofficeclient.entities.PaymentOrder.Status;
import backofficeclient.views.PaymentOrderForm;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import pdfgenerator.PDFGenerator;

/**
 * Manages every functionality about payment orders.
 * @author GCI16_25
 */
public class PaymentOrderController {
    private PaymentOrderForm paymentOrderFrame; 
    private final String session;
    private List<PaymentOrder> paymOrdList;
    private BillForm billFrame;
    private List<Bill> billList;
    
    /**
     * Constructor of payment order controller
     * @param session current JSESSIONID.
     */
    public PaymentOrderController(String session){
        this.session = session;
    }
    
    /**
     * Shows payment orders in the payment order form.
     * 
     */
    public void start(){
        paymentOrderFrame = new PaymentOrderForm(this);
        paymentOrderFrame.setVisible(true);
        //When user closes payment order form, main controller will start.
        paymentOrderFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new MainController(session).start();
            }
        });

        try {
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder?action=get");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.connect();
            
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                line = rd.readLine();
                rd.close();
                Gson gson = new Gson();
                java.lang.reflect.Type POListType = new TypeToken<Collection< PaymentOrder> >(){}.getType();
                paymOrdList = gson.fromJson(line, POListType);
                paymentOrderFrame.setTable(paymOrdList);
                paymentOrderFrame.setVisible(true);
            }
            else if(resCode == 462){
                JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired");
                disconnect();
            }
            else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        }catch (MalformedURLException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);    
        
        }catch (IOException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    /**
     * Manages creation functionality of a payment order.
     * Unpaid bill are shown.
     */
    public void createPaymentOrder(){
        billFrame = new BillForm(this);
        try{
            URL url = new URL("http://localhost:8081/GCI16/Bill?action=get");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.connect();
            int resCode = connection.getResponseCode();
            if(resCode==200){
                InputStream is = connection.getInputStream();                
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line = rd.readLine();
                rd.close();
                Gson gson = new Gson();
                java.lang.reflect.Type BillListType = new TypeToken<Collection< Bill> >(){}.getType();
                List<Bill> list = gson.fromJson(line, BillListType);
                this.billList = list;
                if(list != null){
                    billFrame.setTable(list);
                    billFrame.setVisible(true);
                }
            }
            else if (resCode == 462){
                JOptionPane.showMessageDialog(billFrame,"Session expired");
                disconnect();
            }
            else if(resCode == 500){
                JOptionPane.showMessageDialog(billFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        }   catch (MalformedURLException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
    /**
     * Manages creation functionality.
     * After choosing a bill, back office operator can create a payment order.
     */
    public void createPaymentOrderByBill(){
        if( !ConfirmPanel.showConfirm(billFrame)) return;
        int row = billFrame.getTableSelectedRow();
        Bill b = getBillByRow(row);
        Gson gson = new Gson();
        String gsonString = gson.toJson(b);
        try{
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=create&");
            wr.writeBytes("bill="+gsonString);
            wr.close();
            connection.connect();
            
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                line = rd.readLine();     
                rd.close();
                PaymentOrder p = gson.fromJson(line, PaymentOrder.class);
                paymentOrderFrame.addPaymentOrder(p);
                paymOrdList.add(p);
                JOptionPane.showMessageDialog(billFrame, "Operation successfully completed!");

                billFrame.dispose();
            }
            else if(resCode == 462){
                JOptionPane.showMessageDialog(billFrame,"Session expired");
                disconnect();
            }
            else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        }catch (IOException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }
    
    /**
     * Manages deletion functionality.
     */
    public void deletePaymentOrder(){
        //Ask confirm operation
        if(!ConfirmPanel.showConfirm(paymentOrderFrame)) return;
        int row = paymentOrderFrame.getTableSelectedRow();
        PaymentOrder p = getPaymentOrderByRow(row);
        String gson = new Gson().toJson(p);
        try {
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=delete&");
            wr.writeBytes("paymentOrder="+gson);
            connection.connect();
            
            int resCode = connection.getResponseCode();
            if(resCode == 200){                
                paymOrdList.remove(row);
                paymentOrderFrame.removePaymentOrderByRow(row);
                JOptionPane.showMessageDialog(paymentOrderFrame, "Operation successfully completed!");
            }else if (resCode == 462){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired"); 
              disconnect();
            }
            else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        }catch (MalformedURLException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }

    /**
     * Saves as suspended a payment order.
     */
    public void saveAsSuspendedPaymentOrder(){
       //Ask confirm operation
        if(!ConfirmPanel.showConfirm(paymentOrderFrame)) return; 
        int row = paymentOrderFrame.getTableSelectedRow();
        PaymentOrder p = getPaymentOrderByRow(row);
        String gson = new Gson().toJson(p);
        try{
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=saveAsSuspended&");
            wr.writeBytes("paymentOrder="+gson);
            connection.connect();
 
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                paymentOrderFrame.setPaymentOrderStatus(row, "SUSPENDED");
                PaymentOrder paymOrd = paymOrdList.get(row);
                paymOrd.setStatus(Status.SUSPENDED);
                JOptionPane.showMessageDialog(paymentOrderFrame, "Operation successfully completed!");
            }else if (resCode == 462){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired"); 
              disconnect();
            } 
            else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        } catch (IOException ex) {
            Logger.getLogger(PaymentOrderForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }
    
    /**
     * Saves as paid a payment order.
     */
    public void saveAsPaidPaymentOrder(){
        if(!ConfirmPanel.showConfirm(paymentOrderFrame)) return; 
        int row = paymentOrderFrame.getTableSelectedRow();
        PaymentOrder p = getPaymentOrderByRow(row);
        String gson = new Gson().toJson(p);
        try{
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=saveAsPaid&");
            wr.writeBytes("paymentOrder="+gson);
            connection.connect();
            
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                paymOrdList.remove(row);
                paymentOrderFrame.removePaymentOrderByRow(row);
                JOptionPane.showMessageDialog(paymentOrderFrame, "Operation successfully completed!");
            }else if (resCode == 462){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired");
              disconnect();
            }else if (resCode == 464){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Bad parameter values");
              disconnect();
            }else if (resCode == 465){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Not practicable operation"); 
              disconnect();
            }else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        } catch (MalformedURLException ex) {
                Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }
    
    /**
     * Saves as not pertinent a payment order.
     */
    public void saveAsNotPertinentPaymentOrder() {
        if(!ConfirmPanel.showConfirm(paymentOrderFrame)) return;
        int row = paymentOrderFrame.getTableSelectedRow();
        PaymentOrder p = getPaymentOrderByRow(row);
        //Confirm operation
        String gson = new Gson().toJson(p);
        try{
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=saveAsNotPertinent&");
            wr.writeBytes("paymentOrder="+gson);
            connection.connect(); 
            
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                //Removes that payment order from the table
                paymentOrderFrame.removePaymentOrderByRow(row);
                paymOrdList.remove(row);
                JOptionPane.showMessageDialog(paymentOrderFrame, "Operation successfully completed!");
            }else if (resCode == 462){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired"); 
              disconnect();
            }else if (resCode == 465){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Not practicable operation"); 
              disconnect();
            }else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
            
        }catch (MalformedURLException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }
    
    /**
     * Manages issuing functionality.
     */
    public void issuePaymentOrder() {
        //Confirm operation
        if(!ConfirmPanel.showConfirm(paymentOrderFrame)) return;
        int row = paymentOrderFrame.getTableSelectedRow();
        PaymentOrder p = getPaymentOrderByRow(row);
        Gson gson = new Gson();
        String gsonString = gson.toJson(p);
        try {
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=issue&");
            wr.writeBytes("paymentOrder="+gsonString);
            connection.connect(); 
            
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                //Set issued the selected payment order 
                paymentOrderFrame.setPaymentOrderStatus(row, "ISSUED"); //Modifico la colonna relativa allo stato.
                p.setStatus(Status.ISSUED);
                //Server returns all informations about the issued payment order
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                line = rd.readLine();
                rd.close();
                int protocol = gson.fromJson(line, Integer.class);
                p.setProtocol(protocol);
                //Sets number protocol of payment order in the table 
                paymentOrderFrame.setProtocolNumberByRow(row, protocol);
                // Generates PDF 
                PDFGenerator pdfGen = new PDFGenerator();
                if(pdfGen.generate(p))
                    JOptionPane.showMessageDialog(paymentOrderFrame, "Payment order with protocol " + p.getProtocol() + " has been issued.\nA PDF, with all the information, was created correctly");      
                else
                    JOptionPane.showMessageDialog(paymentOrderFrame, "Payment order with protocol " + p.getProtocol() + " has been issued, but the relative PDF could not be created.");
            }else if (resCode == 462){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired"); 
              disconnect();
            }else if(resCode == 500){
                JOptionPane.showMessageDialog(paymentOrderFrame,"It's not possible to comunicate with server at this moment");
                disconnect();
            }
        
        }catch (MalformedURLException ex) {
            Logger.getLogger(PaymentOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PaymentOrderForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }
    
    /**
     * Reissues a payment order
     */
    public void reissuePaymentOrder(){
        //Confirm operation.
        if(!ConfirmPanel.showConfirm(paymentOrderFrame)) return;
        int row = paymentOrderFrame.getTableSelectedRow();
        PaymentOrder p = getPaymentOrderByRow(row);
        String gson = new Gson().toJson(p);
        try{
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", session);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("action=reissue&");
            wr.writeBytes("paymentOrder="+gson);
            connection.connect();    

            int resCode = connection.getResponseCode();
            if(resCode == 200){
                //Sets issued the selected payment order 
                paymentOrderFrame.setPaymentOrderStatus(row,"ISSUED");
                p.setStatus(Status.ISSUED);
                JOptionPane.showMessageDialog(paymentOrderFrame, "Operation successfully completed!");
                //In this case there is no creation of a new PDF
            }else if (resCode == 462){
              JOptionPane.showMessageDialog(paymentOrderFrame,"Session expired"); 
            }
        } catch (IOException ex) {
            Logger.getLogger(PaymentOrderForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        paymentOrderFrame.clearSelectionTable();
    }
    
    /**
     * Allows to take payment order from the payment order list.
     * It can be done because list and table have the same order.
     * @param row row selected in the table
     * @return payment order in the list, with position 'row'.
     */
    public PaymentOrder getPaymentOrderByRow(int row){
        return paymOrdList.get(row);
    } 
    
    /**
     * Allows to take bill from the list
     * It can be done because list and table have the same order.
     * @param row row selected in the table
     * @return bill in the list, with position 'row'.
     */
    public Bill getBillByRow(int row){
        return billList.get(row);
    }
    
    public void disconnect(){
        BackOfficeLoginController lc = new BackOfficeLoginController();
        lc.start();
        paymentOrderFrame.dispose();
        
    }
}