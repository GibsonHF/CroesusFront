package me.gibson;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.movement.TraverseEvent;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class CroesusFrontScript extends LoopingScript {

    public boolean runScript = false;

    public int actionTick = 0;
    public int xpGained;
    public boolean useHunterCape;
    private BotState botState = BotState.FIND_GUARD;

    public Area.Rectangular sporeArea = new Area.Rectangular(new Coordinate(1916, 1223, 0), new Coordinate(1908, 1231, 0));

    public Area.Rectangular miningArea = new Area.Rectangular(new Coordinate(1897, 1267, 0), new Coordinate(1908, 1256, 0));
    public Area.Rectangular woodcuttingArea = new Area.Rectangular(new Coordinate(1880, 1261, 0), new Coordinate(1888, 1269, 0));
    public Area.Rectangular fishingArea = new Area.Rectangular(new Coordinate(1885, 1229, 0), new Coordinate(1871, 1237, 0));

    public boolean hunter;
    public boolean mining;
    public boolean woodcutting;
    public boolean fishing;

    private List<GuardDetails> guardDetailsList = Arrays.asList(
            new GuardDetails(Arrays.asList("Moulding Varrock guard", "Moulding Lumbridge guard"), Arrays.asList(121760, 121763, 121766, 121769), Arrays.asList(121759, 121762, 121765, 121768), "Mine"),
            new GuardDetails(Arrays.asList("Dead Lumbridge guard", "Dead Varrock guard"), Arrays.asList(121748, 121751, 121754, 121757), Arrays.asList(121747, 121750, 121753, 121756), "Gather"),
            new GuardDetails(Arrays.asList("Colonised Varrock guard", "Colonised Lumbridge guard"), Arrays.asList(28427, 28430, 28433, 28436), Arrays.asList(28426, 28429, 28432, 28435), "Catch"),
            new GuardDetails(Arrays.asList("Decaying Lumbridge guard", "Decaying Varrock guard"), Arrays.asList(28418, 28421, 28415, 28424), Arrays.asList(28417, 28414, 28423, 28420), "Gather")
    );
    enum BotState {
        FIND_GUARD,
        DROP_SPORES, HARVEST_GUARD
    }

    public CroesusFrontScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.loopDelay = 550;
        this.sgc = new GraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
        if (!runScript) {
            return;
        }
        if (botState == BotState.DROP_SPORES) {
            Drop(Client.getLocalPlayer());
        }else
        if (botState == BotState.FIND_GUARD) {
            findGuard(Client.getLocalPlayer());
        } else if (botState == BotState.HARVEST_GUARD) {
            harvestGuard(Client.getLocalPlayer());
        }
    }

    private void findGuard(LocalPlayer localPlayer) {
        if (!mining && !woodcutting && !fishing && !hunter) {
            println("No activity selected. Please select an activity.");
            return;
        }
        GuardDetails guardDetails = getSelectedGuardDetails();
        if (guardDetails == null) {
            return;
        }

        if (mining && !miningArea.contains(localPlayer)) {
            botState = BotState.FIND_GUARD;

            Movement.traverse(NavPath.resolve(miningArea.getRandomWalkableCoordinate()));
            return;
        } else if (woodcutting && !woodcuttingArea.contains(localPlayer)) {
            botState = BotState.FIND_GUARD;

            Movement.traverse(NavPath.resolve(woodcuttingArea.getRandomWalkableCoordinate()));
            return;
        } else if (fishing && !fishingArea.contains(localPlayer)) {
            botState = BotState.FIND_GUARD;

            Movement.traverse(NavPath.resolve(fishingArea.getRandomWalkableCoordinate()));
            return;
        } else if (hunter && !sporeArea.contains(localPlayer)) {
            botState = BotState.FIND_GUARD;
            Movement.traverse(NavPath.resolve(sporeArea.getRandomWalkableCoordinate()));
            return;
        }

        Npc interactingNpc = (Npc) localPlayer.getTarget();
        if (interactingNpc != null && interactingNpc.validate() && guardDetails.getNames().contains(interactingNpc.getName())) {
            if (guardDetails.getEnrichedNpcIds().contains(interactingNpc.getConfigType().getId()) || guardDetails.getNonEnrichedNpcIds().contains(interactingNpc.getConfigType().getId())) {
                println("Continuing to interact with current guard");
                botState = BotState.HARVEST_GUARD;
                return;
            }
        }

        // First, try to find an enriched guard
        if (findAndInteractWithGuard(localPlayer, guardDetails)) {
            return;
        }

        // If no enriched guard is found, try to find a non-enriched guard
        if (findAndInteractWithGuard(localPlayer, guardDetails)) {
            return;
        }
    }

    private GuardDetails getSelectedGuardDetails() {
        if (mining) {
            return guardDetailsList.get(0);
        } else if (woodcutting) {
            return guardDetailsList.get(1);
        } else if (fishing) {
            return guardDetailsList.get(2);
        } else if (hunter) {
            return guardDetailsList.get(3);
        } else {
            return null;
        }
    }
    public static double calculateDistance(Coordinate c1, Coordinate c2) {
        int dx = c2.getX() - c1.getX();
        int dy = c2.getY() - c1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    private boolean findAndInteractWithGuard(LocalPlayer localPlayer, GuardDetails guardDetails) {
        for (String npcName : guardDetails.getNames()) {
            if (fishing || hunter) { // Assuming all NPCs have "guard" in their name
                EntityResultSet<Npc> enrichedNpcQuery = NpcQuery.newQuery().name(npcName).results();
                Npc enrichedGuard = enrichedNpcQuery.stream()
                        .filter(npc -> guardDetails.getEnrichedNpcIds().contains(npc.getConfigType().getId()))
                        .min(Comparator.comparingDouble(npc -> calculateDistance(npc.getCoordinate(), localPlayer.getCoordinate())))
                        .orElse(null);
                if (enrichedGuard != null) {
                    interactWithNpc(enrichedGuard, guardDetails.getAction());
                    return true;
                }

                // If no enriched NPC is found, try to find a non-enriched NPC
                EntityResultSet<Npc> nonEnrichedNpcQuery = NpcQuery.newQuery().name(npcName).results();
                Npc nonEnrichedGuard = nonEnrichedNpcQuery.stream()
                        .filter(npc -> guardDetails.getNonEnrichedNpcIds().contains(npc.getConfigType().getId()))
                        .min(Comparator.comparingDouble(npc -> calculateDistance(npc.getCoordinate(), localPlayer.getCoordinate())))
                        .orElse(null);
                if (nonEnrichedGuard != null) {
                    interactWithNpc(nonEnrichedGuard, guardDetails.getAction());
                    return true;
                }
            } else { // If it's not an NPC, it must be a SceneObject
                // First, try to find an enriched SceneObject
                EntityResultSet<SceneObject> enrichedSceneObjectQuery = SceneObjectQuery.newQuery().name(npcName).results();
                SceneObject enrichedGuard = enrichedSceneObjectQuery.stream()
                        .filter(sceneObject -> guardDetails.getEnrichedNpcIds().contains(sceneObject.getConfigType().getId()))
                        .min(Comparator.comparingDouble(sceneObject -> calculateDistance(sceneObject.getCoordinate(), localPlayer.getCoordinate())))
                        .orElse(null);
                if (enrichedGuard != null) {
                    interactWithSceneObject(enrichedGuard, guardDetails.getAction());
                    return true;
                }

                // If no enriched SceneObject is found, try to find a non-enriched SceneObject
                EntityResultSet<SceneObject> nonEnrichedSceneObjectQuery = SceneObjectQuery.newQuery().name(npcName).results();
                SceneObject nonEnrichedGuard = nonEnrichedSceneObjectQuery.stream()
                        .filter(sceneObject -> guardDetails.getNonEnrichedNpcIds().contains(sceneObject.getConfigType().getId()))
                        .min(Comparator.comparingDouble(sceneObject -> calculateDistance(sceneObject.getCoordinate(), localPlayer.getCoordinate())))
                        .orElse(null);
                if (nonEnrichedGuard != null) {
                    interactWithSceneObject(nonEnrichedGuard, guardDetails.getAction());
                    return true;
                }
            }
        }
        return false;
    }

    private void interactWithNpc(Npc npc, String action) {
        if (Client.getLocalPlayer().getAnimationId() == -1) {
            println(npc.getOptions());
            npc.interact(action);
            println(action);
        }

        println("Found new guard: " + npc.getName());
        botState = BotState.HARVEST_GUARD;
    }

    private void interactWithSceneObject(SceneObject sceneObject, String action) {
        if (Client.getLocalPlayer().getAnimationId() == -1) {
            println(sceneObject.getOptions());
            sceneObject.interact(action);
            println(action);
        }

        println("Found new guard: " + sceneObject.getName());
        botState = BotState.HARVEST_GUARD;
    }

    private void Drop(LocalPlayer localPlayer) {
        if(containsSpores() || containsAlgae() || containsCalcifiedFungus() || containsTimberFungus()) {
            println("Backpack is full, dropping");

            dropItem("Enriched fungal spore");
            dropItem("Fungal spore");
            dropItem("Enriched fungal algae");
            dropItem("Fungal algae");
            dropItem("Enriched calcified fungus");
            dropItem("Calcified fungus");
            dropItem("Enriched timber fungus");
            dropItem("Timber fungus");

            Execution.delayUntil(RandomGenerator.nextInt(10, 20), () -> !containsSpores() && !containsAlgae() && !containsCalcifiedFungus() && !containsTimberFungus());
        } else {
            botState = BotState.FIND_GUARD;
        }
    }

    private void dropItem(String itemName) {
        if(Backpack.getCount(itemName) > 0) {
            if(ActionBar.containsItem(itemName)) {
                ActionBar.useItem(itemName, "Drop");
            } else {
                Backpack.interact(itemName, "Drop");
            }
        }
    }

    public boolean containsTimberFungus() {
        return Backpack.getCount("Enriched timber fungus") > 0 || Backpack.getCount("Timber fungus") > 0;
    }

    public boolean containsCalcifiedFungus() {
        return Backpack.getCount("Enriched calcified fungus") > 0 || Backpack.getCount("Calcified fungus") > 0;
    }

    public boolean containsAlgae() {
        return Backpack.getCount("Enriched fungal algae") > 0 || Backpack.getCount("Fungal algae") > 0;
    }

    public boolean containsSpores() {
        return Backpack.getCount("Enriched fungal spore") > 0 || Backpack.getCount("Fungal spore") > 0;
    }

    private boolean isEnriched(Npc npc) {
        int id = npc.getConfigType().getId();
        return id == 28418 || id == 28421 || id == 28415 || id == 28424 || id == 121760 || id == 121763 || id == 121766 || id == 121769 || id == 28427 || id == 28430 || id == 28433 || id == 28436 || id == 121748 || id == 121751 || id == 121754 || id == 121757;
    }

    private boolean isNonEnriched(Npc npc) {
        int id = npc.getConfigType().getId();
        return id == 28417 || id == 28414 || id == 28423 || id == 28420 || id == 121759 || id == 121762 || id == 121765 || id == 121768 || id == 28426 || id == 28429 || id == 28432 || id == 28435 || id == 121747 || id == 121750 || id == 121753 || id == 121756;
    }




    private void harvestGuard(LocalPlayer localPlayer) {
        if (actionTick >= 1) {
            actionTick = 0;
            return;
        }

        if (actionTick == 0 && useHunterCape && hunter) {
            ActionBar.useItem("Hunter cape (t)", "Activate");
        }

        actionTick++;

        if (Backpack.isFull())
        {
            if(containsSpores() || containsAlgae() || containsCalcifiedFungus() || containsTimberFungus())
                botState = BotState.DROP_SPORES;
                return;
            }


        Npc interactingNpc = (Npc) localPlayer.getTarget();
        if (interactingNpc == null || !interactingNpc.validate() || !isEnriched(interactingNpc)) {
            findGuard(localPlayer);
            return;
        }

        if (!localPlayer.isMoving() && !Backpack.isFull() && localPlayer.getAnimationId() == -1 && interactingNpc == null) {
            findGuard(localPlayer);
        } else if (interactingNpc != null) {
            println("Already interacting with a guard");
        }
    }



    public BotState getBotState() {
        return botState;
    }

}
