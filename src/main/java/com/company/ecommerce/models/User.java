package com.company.ecommerce.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Address address;
    private UserStatus status;

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }

    /**
     * 获取用户全名
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    /**
     * 检查用户是否活跃
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }
}

/**
 * 地址数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Address {

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private boolean primary;
    private AddressType addressType;

    public enum AddressType {
        HOME, OFFICE, BILLING, SHIPPING
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s",
            street, city, state, zipCode, country);
    }
}
