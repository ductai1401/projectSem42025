package projectSem4.com.dto;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name too long")
    private String fullName;

    @Size(max = 20, message = "Phone number too long")
    private String phoneNumber;

    @Size(max = 255, message = "Address too long")
    private String addresses;

    // getters/setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddresses() { return addresses; }
    public void setAddresses(String addresses) { this.addresses = addresses; }
}
