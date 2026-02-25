package me.urninax.flagdelivery.shared.utils;

import me.urninax.flagdelivery.shared.exceptions.BadRequestException;

import java.util.Set;
import java.util.stream.Collectors;

public interface Taggable{
    int MAX_TAGS = 20;

    Set<String> getTags();

    default void addTag(String tag) {
        if (tag == null || tag.isBlank()) return;

        String normalized = tag.trim().toLowerCase();
        Set<String> currentTags = getTags();

        if (currentTags.size() >= MAX_TAGS && !currentTags.contains(normalized)) {
            throw new BadRequestException("Maximum of " + MAX_TAGS + " tags allowed.");
        }
        currentTags.add(normalized);
    }

    default void addTags(Set<String> tags) {
        if (tags != null) tags.forEach(this::addTag);
    }

    default void replaceTags(Set<String> newTags) {
        if (newTags == null) {
            getTags().clear();
            return;
        }

        Set<String> normalized = newTags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase())
                .collect(Collectors.toSet());

        if (normalized.size() > MAX_TAGS) {
            throw new BadRequestException("Cannot provide more than " + MAX_TAGS + " tags.");
        }

        getTags().clear();
        getTags().addAll(normalized);
    }
}
