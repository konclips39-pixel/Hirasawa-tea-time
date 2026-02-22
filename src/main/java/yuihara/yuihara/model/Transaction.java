package yuihara.yuihara.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "transactions") // This creates a new collection in Atlas
public class Transaction {

    @Id
    private String id;
    private String userId;      // The Discord ID
    private String action;      // What happened (e.g., "WORK", "GIFT", "SHOP")
    private long amount;        // How much was gained or lost
    private LocalDateTime time; // The timestamp

    // Default constructor for Spring
    public Transaction() {}

    // Convenience constructor
    public Transaction(String userId, String action, long amount) {
        this.userId = userId;
        this.action = action;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}