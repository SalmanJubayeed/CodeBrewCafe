import database.DatabaseHandler;
import database.Utils;
import models.Admin;
import models.Staff;
import models.User;
import ui.AdminDashboard;
import ui.StaffDashboard;
import database.Logger;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Main {
    private static JFrame mainFrame;
    private static List<User> users;

    public static void main(String[] args) {
        // ✅ Set FlatLaf Look and Feel before any Swing UI code

        UIManager.put("Table.gridColor", Color.LIGHT_GRAY);          // Table grid color
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));  // Change default font
        UIManager.put("Popup.shadowWidth", 12);
        UIManager.put("Button.arc", 20);
        UIManager.put("TextComponent.arc", 15);


        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // You can change to FlatDarkLaf, FlatIntelliJLaf, etc.
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure directories exist
        Utils.ensureDirectoriesExist();

        // Load users
        users = DatabaseHandler.loadUsers();

        // If no users exist, create default admin
        if (users.isEmpty()) {
            users.add(new Admin("admin", "iamadmin"));
            users.add(new Staff("staff", "iamstaff"));
            DatabaseHandler.saveUsers(users);
        }

        // Create and show welcome screen
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("CodeBrew Café");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(400, 300);
            mainFrame.setLocationRelativeTo(null);
            showWelcomeScreen();
            mainFrame.setVisible(true);
        });
    }

    private static void showWelcomeScreen() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to CodeBrew Café");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        welcomePanel.add(welcomeLabel, gbc);

        // Instruction message
        JLabel instructionLabel = new JLabel("Please Login to Access your Cafe");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 20, 10);
        welcomePanel.add(instructionLabel, gbc);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        welcomePanel.add(loginButton, gbc);

        // Exit button
        JButton exitButton = new JButton("Close your Cafe");
        exitButton.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 0;
        gbc.gridy = 3;
        welcomePanel.add(exitButton, gbc);

        // Login action
        loginButton.addActionListener(e -> {
            mainFrame.getContentPane().removeAll();
            showLoginPanel();
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        // Exit action
        exitButton.addActionListener(e -> System.exit(0));

        mainFrame.getContentPane().removeAll();
        mainFrame.add(welcomePanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private static void showLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);



        // Title
        JLabel titleLabel = new JLabel("CodeBrew Café");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(titleLabel, gbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(usernameField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(passwordField, gbc);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(backButton, gbc);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        // Back button action
        backButton.addActionListener(e -> {
            mainFrame.getContentPane().removeAll();
            showWelcomeScreen();
            mainFrame.revalidate();
            mainFrame.repaint();
        });

        // Login action
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            User user = Utils.validateLogin(users, username, password);
            if (user != null) {
                Logger.logLogin(user.getUsername());
                mainFrame.getContentPane().removeAll();
                if (user instanceof Admin) {
                    mainFrame.setSize(800, 600);
                    mainFrame.setLocationRelativeTo(null);
                    mainFrame.add(new AdminDashboard(mainFrame, (Admin) user, Main::showWelcomeScreen));
                } else if (user instanceof Staff) {
                    mainFrame.setSize(800, 600);
                    mainFrame.setLocationRelativeTo(null);
                    mainFrame.add(new StaffDashboard(mainFrame, (Staff) user, Main::showWelcomeScreen));
                }
                mainFrame.revalidate();
                mainFrame.repaint();
            } else {
                Logger.log("Failed login attempt with username: " + username);
                JOptionPane.showMessageDialog(mainFrame, "Invalid username or password",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainFrame.getContentPane().removeAll();
        mainFrame.add(loginPanel);
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}