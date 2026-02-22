package com.yuihara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import economy.Economy; // Make sure this import is here

@SpringBootApplication
// 1. Added "economy" to the Scan list
@ComponentScan(basePackages = {"com.yuihara", "com.yuihara.model", "listeners", "commands", "economy"})
@EnableMongoRepositories(basePackages = {"com.yuihara"}) 
public class YuiharaDashboard {

    @Autowired
    private EconomyRepository economyRepository;

    // 2. This connects the database to your Economy class on startup
    @PostConstruct
    public void initEconomy() {
        Economy.init(economyRepository);
    }

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStoreType", "WINDOWS-ROOT");
        
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };

            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            javax.net.ssl.SSLContext.setDefault(sc); 
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.err.println("SSL Bypass failed, but proceeding anyway...");
        }

        SpringApplication.run(YuiharaDashboard.class, args);
    }
}