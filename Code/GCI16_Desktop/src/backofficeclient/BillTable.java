package backofficeclient;

import backofficeclient.controllers.PaymentOrderController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author carlo
 */
public class BillTable extends javax.swing.JFrame {
    PaymentOrderController paymOrdController;
    String session;
    public BillTable( PaymentOrderController paymOrdController, String session ) {
        initComponents();
        this.paymOrdController = paymOrdController; 
        this.session = session;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        bTable = new javax.swing.JTable();
        createPoButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        bTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                createPoButton.setEnabled(true);
            }
        });
        bTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Debtor", "Year", "Trimester", "Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(bTable);

        createPoButton.setText("Create payment order");
        createPoButton.setEnabled(false);
        createPoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPoButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 683, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createPoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(createPoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(74, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public int getTableSelectedRow(){
        return bTable.getSelectedRow();
    }
        
    private void createPoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPoButtonActionPerformed
        paymOrdController.createPaymentOrderByBill();
        /*if( !ConfirmPanel.showConfirm(this)) return;
        try{
            URL url = new URL("http://localhost:8081/GCI16/PaymentOrder?action=create&bill="+bTable.getValueAt(bTable.getSelectedRow(),0));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Cookie", session);
            int resCode = connection.getResponseCode();
            if(resCode == 200){
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                line = rd.readLine();     
                rd.close();
                Gson gson = new Gson();
                PaymentOrder p = gson.fromJson(line, PaymentOrder.class);
                paymentOrderTable.addPaymentOrder(p);
                ConfirmPanel.showSuccess(this);
            }
            else if(resCode == 462){
                JOptionPane.showMessageDialog(this,"Server not available");
            }
        }catch (IOException ex) {
            Logger.getLogger(BillTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        this.dispose();*/
        
    }//GEN-LAST:event_createPoButtonActionPerformed

    /*Adds a row in the bill table*/
    private void addBill(Bill b){
        Object[] values = new Object[5];
        values[0] = b.getId();
        values[1] = b.getDebtor();
        values[2] = b.getYear();
        values[3] = b.getTrimester();
        values[4] = b.getCost();
        ((DefaultTableModel)bTable.getModel()).addRow(values);
    }
   
    /*Shows the table of all unpaid bills after three months*/ 
    public void setTable(List<Bill> list){
        for(Bill b : list){
            addBill(b);
        }
        /*try{
            URL url = new URL("http://localhost:8081/GCI16/Bill?action=show");
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
                /* Da JSON a collections CLIENT 
                java.lang.reflect.Type BillListType = new TypeToken<Collection< Bill> >(){}.getType();
                List<Bill> list = gson.fromJson(line, BillListType);
                int row = 0;
                int column;
                for(Bill b : list){
                    addBill(b);
                }
                
            }
            else if (resCode == 462){
                JOptionPane.showMessageDialog(this,"Server not available");
                return false;
            }
            
        }catch (MalformedURLException ex) {
            Logger.getLogger(BillTable.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(BillTable.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;*/
    }
    
    
  

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable bTable;
    private javax.swing.JButton createPoButton;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
