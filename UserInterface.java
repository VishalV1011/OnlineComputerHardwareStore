// Superclass: UserInterface
import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class UserInterface {
    protected BasicJDBC db;

    public UserInterface(BasicJDBC db) {
        if (db == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
        this.db = db;
    }

    public abstract void showGUI();

    protected void logout(JFrame frame) {
        try {
            frame.dispose();
            LoginRegisterGUI loginRegister = new LoginRegisterGUI(db);
            loginRegister.showGUI();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error during logout: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Log for debugging
        }
    }
}
