package com.example.moyeorak.dto;

import com.example.moyeorak.entity.User;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String gender;
    private String role;
    private LocalDate birth;
    private String regionName;
    private Long regionId; // ✅ 추가됨

    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getGender().name(),
                user.getRole().name(),
                user.getBirth(),
                user.getRegion() != null ? user.getRegion().getName() : null,
                user.getRegion() != null ? user.getRegion().getId() : null // ✅ 추가됨
        );
    }
}
