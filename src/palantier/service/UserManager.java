package palantier.service;

import palantier.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * UserManager handles all user-related business logic:
 * - Signup (with validation and duplicate checking)
 * - Login (credential verification)
 * - Persistence (save/load users to/from a file)
 *
 * Users are stored in an ArrayList and persisted using Java Object
 * Serialization.
 * The data file is called "users.dat" and is created automatically on first
 * signup.
 */
public class UserManager {

    // File where user data is stored (in the working directory)
    private static final String DATA_FILE = "users.dat";

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
    // FILE PERSISTENCE (Serialization)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Saves the current list of users to the data file using Object Serialization.
     * This is called automatically after every successful signup.
     */
    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
        } catch (IOException e) {
            System.err.println("Warning: Could not save users to file: " + e.getMessage());
        }
    }

    /**
     * Loads users from the data file.
     * If the file does not exist (first run) or is corrupted, returns an empty
     * list.
     */
    @SuppressWarnings("unchecked")
    private ArrayList<User> loadUsers() {
        File file = new File(DATA_FILE);

        // If the file doesn't exist yet, return empty list (first run)
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof ArrayList) {
                return (ArrayList<User>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: Could not load users from file: " + e.getMessage());
            System.err.println("Starting with an empty user list.");
        }

        return new ArrayList<>();
    }
}
