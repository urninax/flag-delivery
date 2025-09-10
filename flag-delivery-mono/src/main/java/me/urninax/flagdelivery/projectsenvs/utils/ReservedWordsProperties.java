package me.urninax.flagdelivery.projectsenvs.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "reserved")
public record ReservedWordsProperties(List<String> keys){}
