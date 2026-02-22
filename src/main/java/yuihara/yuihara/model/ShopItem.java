package yuihara.yuihara.model; // Changed from yuihara.yuihara...

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shopItems")
public class ShopItem {
    @Id
    private String id;
    private String name;
    private long price;
    private String description;
    private String imageUrl;

    public ShopItem() {} // Added default constructor for Spring
    public ShopItem(String name, long price, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters (Keep your existing ones here)
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}