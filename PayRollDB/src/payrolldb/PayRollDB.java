package payrolldb;

public class PayRollDB {

    public static void main(String[] args) {
        // Ito ang mag-a-activate ng Event Dispatch Thread (EDT)
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Tatawagin ang LoginForm para magsimula ang flow
                new LoginForm().setVisible(true);
            }
        });
    }
}