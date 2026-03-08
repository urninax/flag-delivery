package me.urninax.flagdelivery.projectsenvs.services.validation;

import lombok.Getter;

@Getter
public enum KeyType{
    PROJECT("Project"),
    ENVIRONMENT("Environment"),
    CONTEXTKIND( "Context kind"),
    ANY("Any");

    private final String name;

    KeyType(String name){
        this.name = name;
    }
}
