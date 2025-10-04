package me.urninax.flagdelivery.shared.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ErrorMessage{
    private Instant timestamp;
    private int status;

    @JsonProperty("error_code")
    private String errorCode;
    private String message;
    private String path;
}
