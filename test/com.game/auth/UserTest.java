package com.game.auth;

public class UserTest {

    public static void main(String[] args) {
        // Step 1: Create a new User object with dummy values
        User user = new User("Alice", "hashed123:saltABC", "alice@example.com", "user", "123456");

        // Step 2: Test getUsername()
        assert user.getUsername().equals("Alice") : "getUsername() failed";

        // Step 3: Test getPasswordHash()
        assert user.getPasswordHash().equals("hashed123:saltABC") : "getPasswordHash() failed";

        // Step 4: Test getEmail()
        assert user.getEmail().equals("alice@example.com") : "getEmail() failed";

        // Step 5: Test getRole()
        assert user.getRole().equals("user") : "getRole() failed";

        // Step 6: Test getTwoFactorCode()
        assert user.getTwoFactorCode().equals("123456") : "getTwoFactorCode() failed";

        // Step 7: Test suspension flags
        assert !user.isSuspended() : "User should not be suspended by default";
        user.setSuspended(true);
        assert user.isSuspended() : "setSuspended(true) failed";
        user.setSuspended(false);
        assert !user.isSuspended() : "setSuspended(false) failed";

        // Step 8: Test lockout flags
        assert !user.isLockOut() : "User should not be locked out by default";
        user.setLockOut();
        assert user.isLockOut() : "setLockOut() failed";
        user.setUnLock();
        assert !user.isLockOut() : "setUnLock() failed";

        // Step 9: Test rememberMe flags
        user.setRememberMe(true);
        assert user.isRememberMe() : "setRememberMe(true) failed";
        user.setRememberMe(false);
        assert !user.isRememberMe() : "setRememberMe(false) failed";

        // Step 10: Test setters for username, password, email, role, and 2FA
        user.setUsername("Bob");
        assert user.getUsername().equals("Bob") : "setUsername() failed";

        user.setPasswordHash("newHash:newSalt");
        assert user.getPasswordHash().equals("newHash:newSalt") : "setPasswordHash() failed";

        user.setEmail("bob@example.com");
        assert user.getEmail().equals("bob@example.com") : "setEmail() failed";

        user.setRole("admin");
        assert user.getRole().equals("admin") : "setRole() failed";

        user.setTwoFactor("999999");
        assert user.getTwoFactorCode().equals("999999") : "setTwoFactor() failed";

        System.out.println("✅ All User class tests passed.");
    }
}
