package me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment;

import java.util.List;

public record ListAllEnvironmentsRequest(String query, List<String> tags){}
