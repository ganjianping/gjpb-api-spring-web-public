package org.ganjp.blog.util;

/**
 * Demo class to show how to use the PasswordGenerator class.
 */
public class PasswordGeneratorDemo {
    
    public static void main(String[] args) {
        // Create a password generator with default settings
        PasswordGenerator generator = new PasswordGenerator();
        
        System.out.println("===== Password Generator Demo =====\n");
        
        // Generate a password with default length (12 characters)
        String defaultPassword = generator.generatePassword();
        System.out.println("Default password (12 chars): " + defaultPassword);
        
        // Generate a longer password (16 characters)
        String longerPassword = generator.generatePassword(16);
        System.out.println("Longer password (16 chars): " + longerPassword);
        
        // Generate a password and show its BCrypt encoding
        String[] pwdWithEncoding = generator.generatePasswordWithEncoding();
        System.out.println("\nGenerated password: " + pwdWithEncoding[0]);
        System.out.println("BCrypt encoded: " + pwdWithEncoding[1]);
        
        // Verify a password against its encoded version
        boolean matches = generator.matches(pwdWithEncoding[0], pwdWithEncoding[1]);
        System.out.println("Password match verification: " + matches);
        
        // Example for encoding a known password (like 'admin123')
        String knownPassword = "admin123";
        String encodedKnown = generator.encodePassword(knownPassword);
        System.out.println("\nKnown password: " + knownPassword);
        System.out.println("BCrypt encoded: " + encodedKnown);
        
        // Generate admin user SQL
        String adminUsername = "admin";
        String adminPassword = "admin123";  // You might want to use a generated password instead
        String encodedAdminPassword = generator.encodePassword(adminPassword);
        
        System.out.println("\n===== SQL for Admin User Creation =====");
        System.out.println("-- Admin user credentials");
        System.out.println("-- Username: " + adminUsername);
        System.out.println("-- Password: " + adminPassword);
        System.out.println();
        System.out.println("-- Create ADMIN role if it doesn't exist");
        System.out.println("INSERT INTO am_roles (id, code, name, display_order, created_at, updated_at, created_by, updated_by, is_active)");
        System.out.println("SELECT UUID(), 'ADMIN', 'Administrator', 1, NOW(), NOW(), 'system', 'system', 1");
        System.out.println("FROM DUAL");
        System.out.println("WHERE NOT EXISTS (SELECT 1 FROM am_roles WHERE code = 'ADMIN');");
        System.out.println();
        System.out.println("-- Get the role ID for the ADMIN role");
        System.out.println("SET @admin_role_id = (SELECT id FROM am_roles WHERE code = 'ADMIN' LIMIT 1);");
        System.out.println();
        System.out.println("-- Create admin user if it doesn't exist");
        System.out.println("INSERT INTO am_users (id, username, password, display_order, created_at, updated_at, created_by, updated_by, is_active)");
        System.out.println("SELECT ");
        System.out.println("    UUID(), ");
        System.out.println("    '" + adminUsername + "', ");
        System.out.println("    '" + encodedAdminPassword + "', ");
        System.out.println("    1, ");
        System.out.println("    NOW(), ");
        System.out.println("    NOW(), ");
        System.out.println("    'system', ");
        System.out.println("    'system', ");
        System.out.println("    1");
        System.out.println("FROM DUAL");
        System.out.println("WHERE NOT EXISTS (SELECT 1 FROM am_users WHERE username = '" + adminUsername + "');");
        System.out.println();
        System.out.println("-- Get the user ID for the admin user");
        System.out.println("SET @admin_user_id = (SELECT id FROM am_users WHERE username = '" + adminUsername + "' LIMIT 1);");
        System.out.println();
        System.out.println("-- Associate admin user with ADMIN role if not already associated");
        System.out.println("INSERT INTO am_user_roles (user_id, role_id, display_order, created_at, updated_at, created_by, updated_by, is_active)");
        System.out.println("SELECT ");
        System.out.println("    @admin_user_id, ");
        System.out.println("    @admin_role_id, ");
        System.out.println("    1, ");
        System.out.println("    NOW(), ");
        System.out.println("    NOW(), ");
        System.out.println("    'system', ");
        System.out.println("    'system', ");
        System.out.println("    1");
        System.out.println("FROM DUAL");
        System.out.println("WHERE NOT EXISTS (");
        System.out.println("    SELECT 1 FROM am_user_roles ");
        System.out.println("    WHERE user_id = @admin_user_id AND role_id = @admin_role_id");
        System.out.println(");");
    }
}
