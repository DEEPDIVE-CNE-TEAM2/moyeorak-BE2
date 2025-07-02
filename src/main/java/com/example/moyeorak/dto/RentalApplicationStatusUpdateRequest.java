package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalApplicationStatusUpdateRequest {
    @NotBlank(message = "상태는 필수입니다.")
    private String status;
}
