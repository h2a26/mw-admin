package org.hein.entity;

import lombok.Getter;

@Getter
public enum Action {
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    EXECUTE("execute"),
    LIST("list"),
    EXPORT("export"),
    IMPORT("import"),
    APPROVE("approve"),
    REJECT("reject");

    private final String name;

    Action(String name) {
        this.name = name;
    }

    public static Action fromName(String name) {
        for (Action action : values()) {
            if (action.name.equalsIgnoreCase(name)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown action: " + name);
    }
}