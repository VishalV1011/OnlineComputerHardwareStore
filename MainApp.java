import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize the database connection
                BasicJDBC db = new BasicJDBC();

                // Initialize the login/register GUI
                LoginRegisterGUI loginRegister = new LoginRegisterGUI(db);

                // Check and create a root user if none exists
                MainLoginSystem loginSys = new MainLoginSystem(db);
                loginSys.checkAndCreateRootUser();

                // Display the login/register GUI
                loginRegister.showGUI();

            } catch (Exception ex) {
                // Handle unexpected errors during application startup
                JOptionPane.showMessageDialog(
                    null,
                    "An unexpected error occurred while starting the application: " + ex.getMessage(),
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE
                );

                // Log the exception for debugging purposes
                ex.printStackTrace();
            }
        });
    }
}
