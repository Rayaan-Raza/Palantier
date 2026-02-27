package palantier.model;

import java.io.Serializable;

/**
 * User model class for the Palantier application.
 * 
 * Stores user details: full name, email, and password.
 * Implements Serializable so objects can be saved to / loaded from a file.
 * 
 * This class is a simple POJO (Plain Old Java Object) — it only holds data
 * and provides getters. No business logic belongs here.
 */
public class User implements Serializable {

    // Unique version ID for serialization compatibility
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String email;
    private String password;

    /**
     * Creates a new User with the given details.
     *
     * @param fullName the user's full name
     * @param email    the user's email address (used as unique identifier)
     * @param password the user's password (stored as plain text for Sprint 1)
     */
    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // ── toString (useful for debugging) ──────────────────────────────────

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
