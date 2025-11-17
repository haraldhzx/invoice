package com.invoiceapp.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and sanitization to prevent XSS and injection attacks
 */
@Component
public class InputSanitizer {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('|(\\-\\-)|(;)|(\\|\\|)|(\\*)|(<)|(>)|(\\^)|(\\[)|(\\])|(\\{)|(\\})|(%)|(;))",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<script|javascript:|onerror=|onload=|<iframe|<object|<embed",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Sanitize HTML content to prevent XSS attacks
     */
    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        // Remove all HTML tags except safe ones
        return Jsoup.clean(input, Safelist.basic());
    }

    /**
     * Strip all HTML tags from input
     */
    public String stripHtml(String input) {
        if (input == null) {
            return null;
        }
        return Jsoup.clean(input, Safelist.none());
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Check for potential SQL injection patterns
     */
    public boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Check for potential XSS patterns
     */
    public boolean containsXss(String input) {
        if (input == null) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Sanitize filename to prevent directory traversal attacks
     */
    public String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        // Remove path separators and hidden file indicators
        return filename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_")
                .replaceAll("^\\.+", "") // Remove leading dots
                .replaceAll("\\.{2,}", "."); // Replace multiple dots with single dot
    }

    /**
     * Validate that a string doesn't contain malicious patterns
     */
    public boolean isSafe(String input) {
        if (input == null) {
            return true;
        }
        return !containsSqlInjection(input) && !containsXss(input);
    }

    /**
     * Sanitize general text input
     */
    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        // Remove control characters but keep newlines and tabs
        return input.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "")
                .trim();
    }

    /**
     * Validate UUID format
     */
    public boolean isValidUuid(String uuid) {
        if (uuid == null) {
            return false;
        }
        String uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        return uuid.toLowerCase().matches(uuidRegex);
    }

    /**
     * Validate that a number string is actually numeric
     */
    public boolean isValidNumber(String number) {
        if (number == null) {
            return false;
        }
        try {
            Double.parseDouble(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Limit string length to prevent DoS attacks
     */
    public String limitLength(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        return input.length() > maxLength ? input.substring(0, maxLength) : input;
    }

    /**
     * Validate password strength
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
