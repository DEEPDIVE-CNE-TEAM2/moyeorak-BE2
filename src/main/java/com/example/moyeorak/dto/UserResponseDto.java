package com.example.moyeorak.dto;

import com.example.moyeorak.entity.User;
import lombok.*;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private String email;
    private String name;
    private String phone;
    private String gender;
    private String address;
    private String role;

    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getGender().name(),
                user.getAddress(),
                user.getRole().name()
        );
    }
}
