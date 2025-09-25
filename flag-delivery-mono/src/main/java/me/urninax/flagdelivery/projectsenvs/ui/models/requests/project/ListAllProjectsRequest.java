package me.urninax.flagdelivery.projectsenvs.ui.models.requests.project;

import java.util.List;

public record ListAllProjectsRequest(String query, List<String> tags, List<String> keys){}
