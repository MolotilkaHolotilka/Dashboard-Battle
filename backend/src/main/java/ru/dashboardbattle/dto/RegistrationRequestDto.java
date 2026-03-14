package ru.dashboardbattle.dto;

public class RegistrationRequestDto {

    private String email;
    private String password;
    private String companyName;

    public RegistrationRequestDto() {
    }

    public RegistrationRequestDto(String email, String password, String companyName) {
        this.email = email;
        this.password = password;
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
