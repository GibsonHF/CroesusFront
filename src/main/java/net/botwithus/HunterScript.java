package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.SkillUpdateEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Distance;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.TickingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HunterScript extends TickingScript {

    public boolean runScript = false;

    public int actionTick = 0;
    public int xpGained;
    private BotState botState = BotState.FIND_GUARD;
    private Npc currentTarget = null;
    private List<String> targetNpcs = Arrays.asList("Decaying Lumbridge guard", "Decaying Varrock guard");
    private int levelsGained;

    enum BotState {
        FIND_GUARD,
        DROP_SPORES, HARVEST_GUARD
    }

    public HunterScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new GraphicsContext(getConsole(), this);
    }

    public boolean onInitialize() {
        subscribe(SkillUpdateEvent.class, skillUpdateEvent -> {
            if (skillUpdateEvent.getId() == Skills.HUNTER.getId()) {
                xpGained += (skillUpdateEvent.getExperience() - skillUpdateEvent.getOldExperience());
                if (skillUpdateEvent.getOldActualLevel() < skillUpdateEvent.getActualLevel())
                    levelsGained++;
            }
        });
        return super.onInitialize();
    }

    @Override
    public void onTick(LocalPlayer localPlayer) {
        if (!runScript) {
            return;
        }

            if(botState == BotState.FIND_GUARD) {
                findGuard(localPlayer);
            }else
            if(botState == BotState.HARVEST_GUARD) {
                HarvestGuard(localPlayer);
            }else if(botState == BotState.DROP_SPORES){
                DropSpores(localPlayer);
            }
    }

    private void DropSpores(LocalPlayer localPlayer) {

        if(containsSpores())
        {
            println("Backpack is full, dropping spores");

            if(Backpack.getCount("Enriched fungal spore") > 0)
                ActionBar.useItem("Enriched fungal spore", "Drop");

            if(Backpack.getCount("Fungal spore") > 0)
                ActionBar.useItem("Fungal spore", "Drop");

            Execution.delayUntil(RandomGenerator.nextInt(10, 20), () -> !containsSpores());
        }else {
            botState = BotState.FIND_GUARD;
        }
    }



    public boolean containsSpores()
    {
        return Backpack.getCount("Enriched fungal spore") > 0 || Backpack.getCount("Fungal spore") > 0;
    }

    private void HarvestGuard(LocalPlayer localPlayer) {
        if(actionTick >= 1) {
            actionTick = 0;
            return;
        }

        if(actionTick == 0) {
            println("Using Cape");
            ActionBar.useItem("Hunter cape (t)", "Activate");
        }

        actionTick++;
        if(Backpack.isFull() && containsSpores()) {
            botState = BotState.DROP_SPORES;
            return;
        }

        Npc interactingNpc = (Npc) localPlayer.getTarget();
        if(interactingNpc == null || !interactingNpc.validate() || !isEnriched(interactingNpc)) {
            findGuard(localPlayer);
            //!targetNpcs.contains(interactingNpc.getName())
            return;
        }

        if(!localPlayer.isMoving() && !Backpack.isFull() && localPlayer.getAnimationId() == -1 && interactingNpc == null){
            findGuard(localPlayer);
        } else if (interactingNpc != null) {
            println("Already interacting with a guard");
        }
    }

    private boolean isEnriched(Npc npc) {
        int id = npc.getConfigType().getId();
        return id == 28418 || id == 28421 || id == 28415 || id == 28424;
    }

    private boolean isNonEnriched(Npc npc) {
        int id = npc.getConfigType().getId();
        return id == 28417 || id == 28414 || id == 28423 || id == 28420;
    }

    public void findGuard(LocalPlayer localPlayer) {
        Npc interactingNpc = (Npc) localPlayer.getTarget();
        if(interactingNpc != null && interactingNpc.validate() && targetNpcs.contains(interactingNpc.getName()))
        {
            if (isEnriched(interactingNpc)) {
                println("Continuing to interact with current guard");
                botState = botState.HARVEST_GUARD;
                return;
            } else if (isNonEnriched(interactingNpc)) {
                println("Continuing to interact with current guard");
                botState = BotState.HARVEST_GUARD;
                return;
            }
        }

        // First, try to find an enriched guard
        for (String npcName : targetNpcs) {
            EntityResultSet<Npc> sceneObjectQuery = NpcQuery.newQuery().name(npcName).results();
            Npc guard = sceneObjectQuery.stream().filter(this::isEnriched).findFirst().orElse(null);
            if(guard != null) {
                if(localPlayer.getAnimationId() != 6605) {
                    guard.interact("Gather");
                    println("Found new enriched guard");
                }

                botState = BotState.HARVEST_GUARD;
                return;
            }
        }

        // If no enriched guard is found, try to find a non-enriched guard
        for (String npcName : targetNpcs) {
            EntityResultSet<Npc> sceneObjectQuery = NpcQuery.newQuery().name(npcName).results();
            Npc guard = sceneObjectQuery.stream().filter(this::isNonEnriched).findFirst().orElse(null);
            if(guard != null) {
                if(localPlayer.getAnimationId() != 6605) {
                    guard.interact("Gather");
                    println("Found new non-enriched guard");
                }

                botState = BotState.HARVEST_GUARD;
                return;
            }
        }
    }


    public BotState getBotState() {
        return botState;
    }

}
