package com.yuihara;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                // Allow the dashboard routes so you don't get kicked to a login page that doesn't exist
                .requestMatchers("/", "/login**", "/error**", "/css/**", "/dashboard/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/dashboard/main", true)
            );
        return http.build();
    }

    @Bean
    public MongoClient mongoClient() {
        // Updated URI with your credentials
        String uri = "mongodb+srv://konclips39_db_user:OtisDog%2355044@teatimebot.t0jlpsf.mongodb.net/yuihara?retryWrites=true&w=majority";

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(uri))
            .applyToSslSettings(builder -> {
                builder.enabled(true);
                try {
                    // This bypasses the certificate check that causes 'internal_error'
                    builder.context(com.yuihara.config.SSLUtil.getInsecureSSLContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                builder.invalidHostNameAllowed(true);
            })
            .applyToClusterSettings(builder -> 
                builder.serverSelectionTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS))
            .build();

        return MongoClients.create(settings);
    }
}