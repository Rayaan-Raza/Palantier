package palantier;

import palantier.service.UserManager;
import palantier.ui.LoginForm;

import javax.swing.*;

/**
 * Main — Entry point for the Palantier application.
 *
 * Initializes the UserManager (which loads any saved users from file)
 * and opens the LoginForm as the starting window.
 */
public class Main {

    public static void main(String[] args) {

        // Set the system look-and-feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If it fails, the default Java look-and-feel is used — no problem
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Create the shared UserManager instance
        // This loads any existing users from the "users.dat" file
        UserManager userManager = new UserManager();

        // Launch the Login form on the Swing Event Dispatch Thread (EDT)
        // This is the correct way to start a Swing GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm(userManager).setVisible(true);
            }
        });
    }
}
