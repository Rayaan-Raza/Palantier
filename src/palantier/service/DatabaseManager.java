package palantier.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Central MongoDB database connection helper.
 */
public class DatabaseManager {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static synchronized void initializeDatabase() {
        if (mongoClient == null) {
            mongoClient = new MongoClient("localhost", 27017);
            database = mongoClient.getDatabase("palantier_db");
        }
    }

    public static MongoDatabase getDatabase() {
        if (database == null) {
            initializeDatabase();
        }
        return database;
    }
}
