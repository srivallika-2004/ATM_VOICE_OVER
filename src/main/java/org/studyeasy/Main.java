// Main.java
package org.studyeasy;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

// Custom JPanel with background image
class ImagePanel extends JPanel {

    private Image backgroundImage;

    public ImagePanel(String imagePath) {
        try {
            backgroundImage = new ImageIcon(imagePath).getImage(); // Load the image
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Draw the background image scaled to fit the size of the panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

public class Main {
    private static Voice voice;
    private static Account account;
    private static JFrame frame;
    private static JTextArea messageArea;
    private static JTextField inputField;
    private static JButton enterButton;
    private static JPanel buttonPanel;
    private static String currentAction;
    private static String tempAadhaar;
    private static String tempVoterId;
    private static double tempInitialDeposit;

    public static void main(String[] args) {
        // Initialize FreeTTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager vm = VoiceManager.getInstance();
        voice = vm.getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
        } else {
            System.err.println("Cannot allocate voice. Check FreeTTS configuration.");
            return; // Exit if voice allocation failed
        }

        // Initialize GUI
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("ATM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Increased frame size for demonstration

        // Specify the image path here (replace with your image path)
        String imagePath = "C:\\Users\\Srivallika\\Downloads\\atm2.jpg";
        ImagePanel panel = new ImagePanel(imagePath);
        panel.setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 50));
        messageArea.setOpaque(false); // Make JTextArea transparent
        messageArea.setForeground(Color.BLACK); // Set text color to contrast with background

        inputField = new JTextField();
        inputField.setEnabled(false);
        inputField.setFont(new Font("Arial", Font.PLAIN, 30));

        enterButton = new JButton("Enter");
        enterButton.setEnabled(false);
        enterButton.setFont(new Font("Arial", Font.PLAIN, 30));
        enterButton.addActionListener(new EnterButtonActionListener());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false); // Make inputPanel transparent
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(enterButton, BorderLayout.EAST);

        buttonPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel.setOpaque(false); // Make buttonPanel transparent
        buttonPanel.setVisible(false);

        panel.add(messageArea, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.EAST);

        frame.setContentPane(panel);
        frame.setVisible(true);

        // Start the ATM program
        showMainMenu();
    }

    private static void showMainMenu() {
        clearAndSpeak("Welcome to the ATM.\nselect 1 for create new account \n 2 for access existing account \n 3 for exit:");
      //  speak("");

        clearButtons();
        createButton("1 -- > Create New Account", e -> createNewAccount());
        createButton("2 -- > Access Existing Account", e -> accessExistingAccount());
        createButton("3 -- > Exit", e -> exitATM());

        showButtons(true);
    }

    private static void createNewAccount() {
        clearAndSpeak("Please enter your Aadhaar number: ");
        enableInput("aadhaarNumber");
        showButtons(false);
    }

    private static void accessExistingAccount() {
        clearAndSpeak("Please insert your card (Enter account ID): ");
        enableInput("accountId");
        showButtons(false);
    }

    private static void processTransactions() {
        clearAndSpeak("Select your transaction \n 1 for balance enquiry \n 2 for withdrawal \n 3 for deposit\n 4 for change pin \n 5 for mini statement\n 6 for fast cash \n 7 for exit.");

        clearButtons();
        createButton("1 -- >Balance Inquiry", e -> handleTransactionMenu("1"));
        createButton("2 -- >Withdrawal", e -> handleTransactionMenu("2"));
        createButton("3 -- >Deposit", e -> handleTransactionMenu("3"));
        createButton("4 -- >Change PIN", e -> handleTransactionMenu("4"));
        createButton("5 -- >Mini Statement", e -> handleTransactionMenu("5"));
        createButton("6 -- >Fast Cash ($100)", e -> handleTransactionMenu("6"));
        createButton("7 -- >Exit", e -> handleTransactionMenu("7"));

        showButtons(true);
    }

    private static void showMainMenuButtons() {
        clearButtons();
        createButton("1 -- > Create New Account", e -> createNewAccount());
        createButton("2 -- > Access Existing Account", e -> accessExistingAccount());
        createButton("3 -- > Exit", e -> exitATM());
        showButtons(true);
    }

    private static void handleTransactionMenu(String action) {
        switch (action) {
            case "1":
                showBalance();
                showButtons(false);
                showMainMenuButtons();
                break;
            case "2":
                clearAndSpeak("Enter the amount to withdraw: ");
                enableInput("withdraw");
                showButtons(false);
                break;
            case "3":
                clearAndSpeak("Enter the amount to deposit: ");
                enableInput("deposit");
                showButtons(false);
                break;
            case "4":
                clearAndSpeak("Enter new PIN: ");
                enableInput("newPIN");
                showButtons(false);
                break;
            case "5":
                clearAndSpeak("Fetching mini statement...");
                showMiniStatement();
                showMainMenuButtons();
                break;
            case "6":
                processFastCash();
                showMainMenuButtons();
                break;
            case "7":
                clearAndSpeak("Thank you for using our ATM. Goodbye.");
                System.exit(0);
                showButtons(false);
                break;
        }
    }

    private static void returnToMainMenu() {
        clearAndSpeak("Returning to main menu.");
        showMainMenu();
    }

    private static void showBalance() {
        double balance = account.getBalance();
        clearAndSpeak("Your current balance is $" + balance);
        returnToMainMenu();
    }

    private static void showMiniStatement() {
        List<String> miniStatement = account.getMiniStatement();
        clearAndSpeak("Mini Statement:");
        for (String transaction : miniStatement) {
            speak(transaction);
        }
        returnToMainMenu();
    }

    private static void processFastCash() {
        double fastCashAmount = 100.00;
        if (account.fastCash(fastCashAmount)) {
            clearAndSpeak("Fast Cash withdrawal successful. Amount withdrawn: $" + fastCashAmount);
        } else {
            clearAndSpeak("Insufficient balance for Fast Cash withdrawal.");
        }
        returnToMainMenu();
    }

    private static void clearAndSpeak(String text) {
        messageArea.setText(text + "\n");
        speakWithDelay(text);
    }

    private static void speakWithDelay(String text) {
        // Split text by new line characters
        String[] lines = text.split("\n");

        // Create a SwingWorker to handle the speech in a background thread
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (String line : lines) {
                    publish(line); // Send the line to process method
                    try {
                        Thread.sleep(500); // Pause for 1 second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    speak(chunk); // Speak the line
                }
            }
        };

        worker.execute(); // Start the background task
    }

    private static void speak(String text) {
        if (voice != null) {
            voice.speak(text);
        }
    }

    private static void createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        button.addActionListener(actionListener);
        buttonPanel.add(button);
    }

    private static void clearButtons() {
        buttonPanel.removeAll();
    }

    private static void showButtons(boolean visible) {
        buttonPanel.setVisible(visible);
    }

    private static void enableInput(String action) {
        currentAction = action;
        inputField.setEnabled(true);
        inputField.requestFocus();
        enterButton.setEnabled(true);
    }

    private static class EnterButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputField.getText().trim();
            inputField.setText("");
            handleInput(input);
        }
    }

    private static void handleInput(String input) {
        switch (currentAction) {
            case "aadhaarNumber":
                if (validateAadhaar(input)) {
                    tempAadhaar = input;
                    clearAndSpeak("Aadhaar number validated. Please enter your Voter ID: ");
                    enableInput("voterId");
                } else {
                    clearAndSpeak("Invalid Aadhaar number. Please enter a valid Aadhaar number: ");
                    enableInput("aadhaarNumber");
                }
                break;
            case "voterId":
                if (validateVoterId(input)) {
                    tempVoterId = input;
                    clearAndSpeak("Voter ID validated. Please enter your initial deposit: ");
                    enableInput("initialDeposit");
                } else {
                    clearAndSpeak("Invalid Voter ID. Please enter a valid Voter ID: ");
                    enableInput("voterId");
                }
                break;
            case "initialDeposit":
                tempInitialDeposit = Double.parseDouble(input);
                clearAndSpeak("Please enter your PIN to create : ");
                enableInput("accountPINCreation");
                break;
            case "accountPINCreation":
                if (input.length() == 4 && input.matches("\\d+")) {
                    account = Account.createAccount(tempInitialDeposit, input);
                    clearAndSpeak("Account created successfully. Your account ID is: " + account.getAccountId());
                    processTransactions(); // Return to main menu after account creation
                } else {
                    clearAndSpeak("Invalid PIN format. Please enter exactly 4 digits for your PIN: ");
                    enableInput("accountPINCreation");
                }
                break;
            case "accountId":
                int accountId = Integer.parseInt(input);
                account = Account.getAccount(accountId);
                if (account != null) {
                    clearAndSpeak("Account found. Please enter your PIN: ");
                    enableInput("accountPIN");
                } else {
                    clearAndSpeak("Account not found. Returning to main menu.");
                    returnToMainMenu();
                }
                break;
            case "accountPIN":
                if (input.length() == 4 && input.matches("\\d+")) {
                    if (account.validatePIN(input)) {
                        clearAndSpeak("PIN validated successfully.");
                        processTransactions();
                    } else {
                        clearAndSpeak("Invalid PIN. Returning to main menu.");
                        returnToMainMenu();
                    }
                } else {
                    clearAndSpeak("Invalid PIN format. Please enter exactly 4 digits for your PIN: ");
                    enableInput("accountPIN");
                }
                break;
            case "withdraw":
                double amountToWithdraw = Double.parseDouble(input);
                if (amountToWithdraw > 0) {
                    if (account.withdraw(amountToWithdraw)) {
                        String successMessage = "Withdrawal successful. Amount withdrawn: $" + amountToWithdraw;
                        messageArea.append(successMessage + "\n");  // Display message in the message area
                        clearAndSpeak(successMessage);
                    } else {
                        clearAndSpeak("Insufficient balance or invalid amount.");
                    }
                } else {
                    clearAndSpeak("Invalid withdrawal amount. Please enter a positive amount.");
                }
                returnToMainMenu(); // Return to main menu after withdrawal
                break;
            case "deposit":
                double amountToDeposit = Double.parseDouble(input);
                if (amountToDeposit > 0) {
                    account.deposit(amountToDeposit);
                    clearAndSpeak("Deposit successful. Amount deposited: $" + amountToDeposit);
                } else {
                    clearAndSpeak("Invalid deposit amount. Please enter a positive amount.");
                }
                returnToMainMenu(); // Return to main menu after deposit
                break;
            case "newPIN":
                if (input.length() == 4 && input.matches("\\d+")) {
                    account.changePIN(input);
                    clearAndSpeak("PIN changed successfully.");
                } else {
                    clearAndSpeak("Invalid PIN format. Please enter exactly 4 digits for your PIN: ");
                }
                returnToMainMenu(); // Return to main menu after PIN change
                break;
            default:
                clearAndSpeak("Invalid action. Returning to main menu.");
                returnToMainMenu();
                break;
        }
    }

    private static void exitATM() {
        clearAndSpeak("Thank you for using our ATM. Goodbye.");
        System.exit(0);
    }

    private static boolean validateAadhaar(String aadhaar) {
        return aadhaar.matches("\\d{12}");
    }

    private static boolean validateVoterId(String voterId) {
        return voterId.matches("[A-Z]{3}[0-9]{7}");
    }
}