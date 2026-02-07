package me.urninax.flagdelivery.flags.ui.requests;

import java.util.List;

public record ListAllFlagsRequest(String query, List<String> tags, String maintainer, String type){
}
