package me.urninax.flagdelivery.projectsenvs.models.project;

import java.util.regex.Pattern;

public enum CasingConvention{
    NONE("", "None"),
    CAMEL("^[a-z]+(?:[A-Z][a-z0-9]*)*$", "camelCase"),
    PASCAL("^[A-Z][a-z0-9]*(?:[A-Z][a-z0-9]*)*$", "PascalCase"),
    SNAKE("^[a-z0-9]+(?:_[a-z0-9]+)*$", "snake_case"),
    KEBAB("^[a-z0-9]+(?:-[a-z0-9]+)*$", "kebab-case");

    private final Pattern pattern;
    private final String label;

    CasingConvention(String regex, String label){
        this.pattern = Pattern.compile(regex);
        this.label = label;
    }

    public boolean matches(String input){
        if(this == NONE) return true;
        return input != null && pattern.matcher(input).matches();
    }

    public String description(){
        return switch(this){
            case NONE -> "no particular rules";
            case CAMEL -> "starts lowercase, words joined, e.g. featureFlag";
            case PASCAL -> "starts uppercase, e.g. FeatureFlag";
            case SNAKE -> "lowercase, underscore, e.g. feature_flag";
            case KEBAB -> "lowercase, dash, e.g. feature-flag";
        };
    }


    @Override
    public String toString(){
        return label;
    }
}
