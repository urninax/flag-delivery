package me.urninax.flagdelivery.user.utils;

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
    private String message;
    private String path;
}
