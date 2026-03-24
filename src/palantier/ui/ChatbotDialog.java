package palantier.ui;

import palantier.model.User;
import palantier.service.ChatbotService;
import palantier.service.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * ChatbotDialog — An AI assistant chat interface for Palantier.
 */
public class ChatbotDialog extends JDialog {

    private User currentUser;
    private TaskManager taskManager;
    private ChatbotService chatbotService;

    private JTextArea chatHistory;
    private JTextField messageInput;
    private JButton sendButton;

    public ChatbotDialog(JFrame parent, User currentUser, TaskManager taskManager) {
        super(parent, "AI Assistant", false);
        this.currentUser = currentUser;
        this.taskManager = taskManager;
        this.chatbotService = new ChatbotService();

        initializeUI();
    }

    private void initializeUI() {
        setSize(480, 600);
        setLocationRelativeTo(getParent());
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UITheme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Center panel for chat history
        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        chatHistory.setLineWrap(true);
        chatHistory.setWrapStyleWord(true);
        chatHistory.setBackground(UITheme.BG_INPUT);
        chatHistory.setForeground(UITheme.TEXT_PRIMARY);
        chatHistory.setFont(UITheme.FONT_BODY);
        chatHistory.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatHistory);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true));
        UITheme.styleScrollPane(scrollPane);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for input
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setOpaque(false);

        messageInput = UITheme.createStyledTextField(0);
        messageInput.setPreferredSize(new Dimension(0, 42));
        messageInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        bottomPanel.add(messageInput, BorderLayout.CENTER);

        sendButton = UITheme.createPrimaryButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 42));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Initial greeting
        appendMessage("Assistant", "Hello " + currentUser.getFullName() + "! I can help you with your tasks. What's on your mind?");
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        appendMessage("You", text);
        messageInput.setText("");
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);

        // Call the service asynchronously
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Pass null for apiKey so ChatbotService uses the hardcoded DEFAULT_API_KEY
                return chatbotService.getChatbotResponse(text, null, currentUser, taskManager);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    appendMessage("Assistant", response);
                } catch (Exception e) {
                    appendMessage("System", "Error: " + e.getMessage());
                } finally {
                    messageInput.setEnabled(true);
                    sendButton.setEnabled(true);
                    messageInput.requestFocus();
                }
            }
        };
        worker.execute();
    }

    private void appendMessage(String sender, String message) {
        chatHistory.append(sender + ":\n");
        chatHistory.append(message + "\n\n");
        // Scroll to bottom
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
    }
}
