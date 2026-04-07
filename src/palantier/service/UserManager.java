package palantier.service;

import palantier.model.User;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * UserManager handles all user-related business logic:
 * - Signup (with validation and duplicate checking)
 * - Login (credential verification)
 * - Persistence (save/load users to/from SQLite)
 *
 * Users are cached in memory and persisted to SQLite.
 */
public class UserManager {

    // Simple regex pattern for basic email validation
    // Matches: something@something.something
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    // In-memory list of all registered users
    private ArrayList<User> users;

    /**
     * Creates a new UserManager and loads existing users from the data file.
     * If no data file exists yet (first run), starts with an empty list.
     */
    public UserManager() {
        DatabaseManager.initializeDatabase();
        users = loadUsers();
    }

    // ══════════════════════════════════════════════════════════════════════
    // SIGNUP
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Attempts to register a new user.
     *
     * Validates all input, checks for duplicates, and saves if everything is OK.
     *
     * @param fullName        the user's full name
     * @param email           the user's email
     * @param password        the chosen password
     * @param confirmPassword the password confirmation
     * @return null if signup succeeded, or an error message string if it failed
     */
    public String signup(String fullName, String email, String password, String confirmPassword) {

        // 1. Check for empty fields
        if (fullName.trim().isEmpty() || email.trim().isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            return "All fields are required. Please fill in every field.";
        }

        // 2. Validate email format
        if (!isValidEmail(email.trim())) {
            return "Invalid email format. Please enter a valid email address.";
        }

        // 3. Validate password length (minimum 6 characters)
        if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }

        // 4. Check that passwords match
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match. Please try again.";
        }

        // 5. Check for duplicate email
        if (findUserByEmail(email.trim()) != null) {
            return "An account with this email already exists.";
        }

        // All checks passed — create user, add to list, and save
        User newUser = new User(fullName.trim(), email.trim(), password);
        users.add(newUser);
        saveUsers();

        return null; // null means success (no error)
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Attempts to log in a user with the given credentials.
     *
     * @param email    the email entered by the user
     * @param password the password entered by the user
     * @return the User object if login succeeded, or null if it failed
     *         (check getLoginError() for the specific error message)
     */
    public User login(String email, String password) {

        // Check for empty fields
        if (email.trim().isEmpty() || password.isEmpty()) {
            lastLoginError = "Please enter both email and password.";
            return null;
        }

        // Find user by email
        User user = findUserByEmail(email.trim());

        if (user == null) {
            lastLoginError = "Account does not exist. Please sign up first.";
            return null;
        }

        // Verify password
        if (!user.getPassword().equals(password)) {
            lastLoginError = "Incorrect password. Please try again.";
            return null;
        }

        // Login successful
        lastLoginError = null;
        return user;
    }

    // Stores the last login error message so the UI can display it
    private String lastLoginError;

    /**
     * Returns the error message from the most recent failed login attempt.
     * Returns null if the last login was successful.
     */
    public String getLoginError() {
        return lastLoginError;
    }

    // ══════════════════════════════════════════════════════════════════════
    // USER LIST ACCESS (for task assignment dropdowns)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Returns a copy of the full user list.
     * Used by the UI to populate assignee dropdowns in task forms.
     *
     * @return ArrayList of all registered Users
     */
    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Returns true if a user with this email exists.
     */
    public boolean isValidUserEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return findUserByEmail(email.trim()) != null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Checks if the given email matches a basic email pattern.
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Searches the user list for a user with the given email (case-insensitive).
     *
     * @return the matching User, or null if not found
     */
    private User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATABASE PERSISTENCE (SQLite)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Saves the current list of users to the data file using Object Serialization.
     * This is called automatically after every successful signup.
     */
    private void saveUsers() {
        try {
            MongoCollection<Document> collection = DatabaseManager.getDatabase().getCollection("users");
            collection.deleteMany(new Document());
            
            if (users.isEmpty()) {
                return;
            }
            
            List<Document> docs = new ArrayList<>();
            for (User user : users) {
                Document doc = new Document("email", user.getEmail())
                        .append("full_name", user.getFullName())
                        .append("password", user.getPassword());
                docs.add(doc);
            }
            collection.insertMany(docs);
        } catch (Exception e) {
            System.err.println("Warning: Could not save users to database: " + e.getMessage());
        }
    }

    /**
     * Loads users from the data file.
     * If the file does not exist (first run) or is corrupted, returns an empty
     * list.
     */
    private ArrayList<User> loadUsers() {
        ArrayList<User> loaded = new ArrayList<>();
        try {
            MongoCollection<Document> collection = DatabaseManager.getDatabase().getCollection("users");
            for (Document doc : collection.find()) {
                loaded.add(new User(
                        doc.getString("full_name"),
                        doc.getString("email"),
                        doc.getString("password")));
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load users from database: " + e.getMessage());
        }
        return loaded;
    }
}
