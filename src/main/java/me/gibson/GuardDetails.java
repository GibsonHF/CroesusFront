package me.gibson;

import net.botwithus.rs3.game.Area;

import java.util.List;

public class GuardDetails {
    private List<String> names;
    private List<Integer> enrichedNpcIds;
    private List<Integer> nonEnrichedNpcIds;
    private String action;

    public GuardDetails(List<String> names, List<Integer> enrichedNpcIds, List<Integer> nonEnrichedNpcIds, String action) {
        this.names = names;
        this.enrichedNpcIds = enrichedNpcIds;
        this.nonEnrichedNpcIds = nonEnrichedNpcIds;
        this.action = action;
    }

    public List<String> getNames() {
        return names;
    }

    public List<Integer> getEnrichedNpcIds() {
        return enrichedNpcIds;
    }

    public List<Integer> getNonEnrichedNpcIds() {
        return nonEnrichedNpcIds;
    }

    public String getAction() {
        return action;
    }
}