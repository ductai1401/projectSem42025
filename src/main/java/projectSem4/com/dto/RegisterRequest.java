package projectSem4.com.dto;

import java.io.Serializable;

public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L; // serialVersionUID để đảm bảo tính tương thích

    private String fullName;

    private String email;

    private String password;

    private String confirmPassword;

    private String phoneNumber;

    public RegisterRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
