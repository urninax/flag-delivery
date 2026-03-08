package me.urninax.flagdelivery.projectsenvs.utils;

import me.urninax.flagdelivery.projectsenvs.services.validation.KeyType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "reserved")
public record ReservedWordsProperties(Map<KeyType, List<String>> types){}
