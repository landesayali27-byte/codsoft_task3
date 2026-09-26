import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// ==========================================
// 1. BANK ACCOUNT MODEL (Requirement 4 & 5)
// ==========================================
class BankAccount {
    private double balance;
    private final String accountNumber;
    private final String accountHolder;

    public BankAccount(String accountNumber, String accountHolder, double initialBalance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = Math.max(0.0, initialBalance);
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public synchronized boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    public synchronized boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }
}

// ==========================================
// 2. ATM MACHINE INTERFACE (Requirement 1, 2, 3, 6, 7)
// ==========================================
public class ATMInterface extends JFrame {

    // Palette Colors
    private static final Color BG_DARK = new Color(18, 20, 29);
    private static final Color CARD_BG = new Color(28, 32, 47);
    private static final Color ACCENT_BLUE = new Color(66, 133, 244);
    private static final Color ACCENT_GREEN = new Color(52, 168, 83);
    private static final Color ACCENT_RED = new Color(234, 67, 53);
    private static final Color TEXT_WHITE = new Color(245, 247, 250);
    private static final Color TEXT_MUTED = new Color(140, 147, 168);
    private static final Color BORDER_COLOR = new Color(42, 48, 71);

    // Backend Connection
    private final BankAccount userAccount;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

    // UI Elements
    private JLabel balanceDisplayLabel;
    private JTextField amountInputField;
    private JTextArea transactionLogArea;

    public ATMInterface(BankAccount account) {
        this.userAccount = account;

        setTitle("NextGen Secure ATM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // Top Navigation / Account Info
        add(createTopBanner(), BorderLayout.NORTH);

        // Center Content (Interactive Terminal & Statement Logs)
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 15, 0));
        mainContent.setBackground(BG_DARK);
        mainContent.setBorder(new EmptyBorder(0, 20, 15, 20));

        mainContent.add(createActionsPanel());
        mainContent.add(createHistoryPanel());

        add(mainContent, BorderLayout.CENTER);

        // Initialize state
        updateBalanceDisplay();
        logTransaction("System Session started. Welcome, " + userAccount.getAccountHolder() + ".");
    }

    private JPanel createTopBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(CARD_BG);
        banner.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("ATM BANKING TERMINAL");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);

        JLabel accountInfo = new JLabel("User: " + userAccount.getAccountHolder() + "  |  A/C: " + userAccount.getAccountNumber());
        accountInfo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        accountInfo.setForeground(TEXT_MUTED);

        banner.add(title, BorderLayout.WEST);
        banner.add(accountInfo, BorderLayout.EAST);
        return banner;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Available Balance Badge
        JLabel balTitle = new JLabel("AVAILABLE BALANCE");
        balTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        balTitle.setForeground(TEXT_MUTED);

        balanceDisplayLabel = new JLabel("$0.00");
        balanceDisplayLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        balanceDisplayLabel.setForeground(TEXT_WHITE);

        // Amount Input Section
        JLabel enterLabel = new JLabel("Enter Amount ($):");
        enterLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        enterLabel.setForeground(TEXT_MUTED);

        amountInputField = new JTextField();
        amountInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        amountInputField.setBackground(new Color(16, 18, 25));
        amountInputField.setForeground(TEXT_WHITE);
        amountInputField.setCaretColor(TEXT_WHITE);
        amountInputField.setFont(new Font("SansSerif", Font.BOLD, 18));
        amountInputField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        // Quick Pick Chips
        JPanel quickChips = new JPanel(new GridLayout(1, 3, 8, 0));
        quickChips.setOpaque(false);
        quickChips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        quickChips.add(createChipButton(20));
        quickChips.add(createChipButton(50));
        quickChips.add(createChipButton(100));

        // Main Action Buttons
        JButton depositBtn = createActionButton("Deposit Funds", ACCENT_GREEN);
        depositBtn.addActionListener(e -> handleDeposit());

        JButton withdrawBtn = createActionButton("Withdraw Cash", ACCENT_RED);
        withdrawBtn.addActionListener(e -> handleWithdraw());

        JButton balanceBtn = createActionButton("Check Live Balance", ACCENT_BLUE);
        balanceBtn.addActionListener(e -> handleCheckBalance());

        panel.add(balTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(balanceDisplayLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(enterLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(amountInputField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(quickChips);
        panel.add(Box.createVerticalStrut(15));
        panel.add(depositBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(withdrawBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(balanceBtn);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Audit & Transaction Activity");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(TEXT_WHITE);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        transactionLogArea = new JTextArea();
        transactionLogArea.setEditable(false);
        transactionLogArea.setBackground(new Color(16, 18, 25));
        transactionLogArea.setForeground(new Color(190, 195, 210));
        transactionLogArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        transactionLogArea.setMargin(new java.awt.Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(transactionLogArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return btn;
    }

    private JButton createChipButton(int amount) {
        JButton chip = new JButton("+$" + amount);
        chip.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chip.setBackground(new Color(36, 42, 60));
        chip.setForeground(TEXT_WHITE);
        chip.setFocusPainted(false);
        chip.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.addActionListener(e -> amountInputField.setText(String.valueOf(amount)));
        return chip;
    }

    // ==========================================
    // TRANSACTION METHODS & VALIDATIONS
    // ==========================================
    private void handleDeposit() {
        Double amount = parseInputAmount();
        if (amount == null) return;

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Deposit amount must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = userAccount.deposit(amount);
        if (success) {
            updateBalanceDisplay();
            logTransaction("CREDIT: " + currencyFormatter.format(amount) + " deposited successfully.");
            amountInputField.setText("");
            JOptionPane.showMessageDialog(this, "Successfully deposited " + currencyFormatter.format(amount) + "!", "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Deposit failed. Invalid amount.", "Transaction Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleWithdraw() {
        Double amount = parseInputAmount();
        if (amount == null) return;

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Withdrawal amount must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (amount > userAccount.getBalance()) {
            JOptionPane.showMessageDialog(this, "Insufficient balance! Available: " + currencyFormatter.format(userAccount.getBalance()), "Declined", JOptionPane.ERROR_MESSAGE);
            logTransaction("DECLINED: Attempted withdrawal of " + currencyFormatter.format(amount) + " (Insufficient Funds).");
            return;
        }

        boolean success = userAccount.withdraw(amount);
        if (success) {
            updateBalanceDisplay();
            logTransaction("DEBIT: " + currencyFormatter.format(amount) + " withdrawn.");
            amountInputField.setText("");
            JOptionPane.showMessageDialog(this, "Please take your cash: " + currencyFormatter.format(amount), "Withdrawal Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Withdrawal failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckBalance() {
        double currentBal = userAccount.getBalance();
        updateBalanceDisplay();
        logTransaction("INQUIRY: Current balance checked -> " + currencyFormatter.format(currentBal));
        JOptionPane.showMessageDialog(this, "Current Account Balance: " + currencyFormatter.format(currentBal), "Balance Inquiry", JOptionPane.INFORMATION_MESSAGE);
    }

    private Double parseInputAmount() {
        String text = amountInputField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount first.", "Input Missing", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value.", "Invalid Format", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void updateBalanceDisplay() {
        balanceDisplayLabel.setText(currencyFormatter.format(userAccount.getBalance()));
    }

    private void logTransaction(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        transactionLogArea.append("[" + timestamp + "] " + message + "\n");
        transactionLogArea.setCaretPosition(transactionLogArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        // Instantiate the user's BankAccount with starting balance
        BankAccount account = new BankAccount("ACC-9042817", "Sayali Lande", 1500.00);

        // Run GUI
        SwingUtilities.invokeLater(() -> {
            ATMInterface atm = new ATMInterface(account);
            atm.setVisible(true);
        });
    }
}