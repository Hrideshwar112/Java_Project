package com.example.shorturl.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlService {

    // In-memory store: shortCode -> originalUrl
    private final Map<String, String> store = new ConcurrentHashMap<>();

    private final SecureRandom random = new SecureRandom();
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;

    public String shorten(String originalUrl) {
        // Generate small unique code
        String code;
        int attempts = 0;
        do {
            code = randomCode();
            attempts++;
            // safety break (very unlikely)
            if (attempts > 10_000) throw new RuntimeException("Unable to generate unique code");
        } while (store.containsKey(code));
        store.put(code, originalUrl);
        return code;
    }

    public String resolve(String code) {
        return store.get(code);
    }

    public boolean exists(String code) {
        return store.containsKey(code);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }

    // Optional: allow custom alias
    public boolean createWithAlias(String alias, String originalUrl) {
        if (alias == null || alias.isBlank()) return false;
        if (store.containsKey(alias)) return false;
        store.put(alias, originalUrl);
        return true;
    }
}
