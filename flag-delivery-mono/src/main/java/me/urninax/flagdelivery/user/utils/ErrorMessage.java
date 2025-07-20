package me.urninax.flagdelivery.user.utils;

import lombok.*;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ErrorMessage{
    private String path;
    private String message;
    private Instant instant;
}
