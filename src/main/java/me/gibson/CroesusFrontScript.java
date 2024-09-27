package me.gibson;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Equipment;
import net.botwithus.api.game.hud.inventories.EquipmentInventory;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.influx.InfluxGEEvent;
import net.botwithus.rs3.events.impl.influx.InfluxKillEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.login.LoginManager;
import net.botwithus.rs3.game.login.World;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.movement.TraverseEvent;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.builders.worlds.WorldQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.characters.player.Player;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.*;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;
import net.botwithus.rs3.util.Regex;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Pattern hunterCapePattern = Pattern.compile("Hunter cape( \\(t\\))?|Hooded hunter cape( \\(t\\))?");
    public long startTime;

    private List<GuardDetails> guardDetailsList = Arrays.asList(
            new GuardDetails(Arrays.asList("Moulding Varrock guard", "Moulding Lumbridge guard"), Arrays.asList(121760, 121763, 121766, 121769), Arrays.asList(121759, 121762, 121765, 121768), "Mine"),
            new GuardDetails(Arrays.asList("Dead Lumbridge guard", "Dead Varrock guard"), Arrays.asList(121748, 121751, 121754, 121757), Arrays.asList(121747, 121750, 121753, 121756), "Gather"),
            new GuardDetails(Arrays.asList("Colonised Varrock guard", "Colonised Lumbridge guard"), Arrays.asList(28427, 28430, 28433, 28436), Arrays.asList(28426, 28429, 28432, 28435), "Catch"),
            new GuardDetails(Arrays.asList("Decaying Varrock guard", "Decaying Lumbridge guard"), Arrays.asList(28418, 28421, 28415, 28424), Arrays.asList(28417, 28414, 28423, 28420), "Gather")
    );
    boolean Hopping;
    private long startHopTime = System.currentTimeMillis();
    public int timesHopped;


    public String getRunTime() {
        long time = System.currentTimeMillis() - startTime;
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
   public Random random = new Random();

    enum BotState {
        FIND_GUARD,
        DROP_SPORES, HARVEST_GUARD
    }

    public CroesusFrontScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.loopDelay = 550;
        startTime = System.currentTimeMillis();
        this.sgc = new GraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
        if (!runScript) {
            return;
        }
        Player player = Client.getLocalPlayer();

        if(Client.getGameState() != Client.GameState.LOGGED_IN || player == null)
            return;

        EntityResultSet<Npc> Blessing = NpcQuery.newQuery().name("Divine blessing").option("Capture").results();
        if(Blessing.nearest() != null) {
            Blessing.nearest().interact("Capture");
            Execution.delay(RandomGenerator.nextInt(3000,7000));
        }
        EntityResultSet<Npc> serensprit = NpcQuery.newQuery().name("Seren spirit").option("Capture").results();
        if(serensprit.nearest() != null) {
            serensprit.nearest().interact("Capture");
            Execution.delay(RandomGenerator.nextInt(3000,7000));
        }  
        ResultSet<Component> treasureKey = ComponentQuery.newQuery(1473).componentIndex(5).item(24154).option("Claim key").results();
        if(treasureKey.first() != null) {
            treasureKey.first().interact("Claim key");
            Execution.delay(RandomGenerator.nextInt(3000,7000));
        }
        if(Hopping && (System.currentTimeMillis() - startHopTime) >= ((random.nextInt(2 * 60 * 60 * 1000) + 60 * 60 * 1000))) {
            hopworlds();
            Execution.delayUntil(10000, ()->Client.getGameState() == Client.GameState.LOGGED_IN);
            startHopTime = System.currentTimeMillis(); // Reset the start time after hopping
            timesHopped += 1;
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

    public void hopworlds() {
        if(LoginManager.isLoginInProgress()) {
            return;
        }
        final WorldQuery worlds = WorldQuery.newQuery().members().population(100, 1300).ping(1, 350).mark();
        World world = worlds.results().random();
        if(world != null) {
            LoginManager.hopWorld(world);
        }
        Execution.delayWhile(RandomGenerator.nextInt(5000, 10000), () -> LoginManager.isLoginInProgress());
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
    private boolean findAndInteractWithGuard(LocalPlayer localPlayer, GuardDetails guardDetails) {
        List<Npc> npcs = new ArrayList<>();
        List<SceneObject> sceneObjects = new ArrayList<>();

        // Gather all NPCs or SceneObjects
        for (String npcName : guardDetails.getNames()) {
            if (fishing || hunter) {
                EntityResultSet<Npc> npcQuery = NpcQuery.newQuery().name(npcName).results();
                for (Npc npc : npcQuery) {
                    npcs.add(npc);
                }
            } else if (mining || woodcutting) {
                EntityResultSet<SceneObject> sceneObjectQuery = SceneObjectQuery.newQuery().name(npcName).results();
                for (SceneObject sceneObject : sceneObjectQuery) {
                    sceneObjects.add(sceneObject);
                }
            }
        }

        // Sort NPCs or SceneObjects based on whether they are enriched and their distance to the player
        Npc targetNpc = npcs.stream()
                .sorted(Comparator.comparing((Npc npc) -> !guardDetails.getEnrichedNpcIds().contains(npc.getConfigType().getId()))
                        .thenComparing(npc -> npc.distanceTo(localPlayer.getCoordinate())))
                .findFirst()
                .orElse(null);

        SceneObject targetSceneObject = sceneObjects.stream()
                .sorted(Comparator.comparing((SceneObject sceneObject) -> !guardDetails.getEnrichedNpcIds().contains(sceneObject.getConfigType().getId()))
                        .thenComparing(sceneObject -> sceneObject.distanceTo(localPlayer.getCoordinate())))
                .findFirst()
                .orElse(null);

        if (targetNpc != null) {
            interactWithNpc(targetNpc, guardDetails.getAction());
            return true;
        }

        if (targetSceneObject != null) {
            interactWithSceneObject(targetSceneObject, guardDetails.getAction());
            return true;
        }

        return false;
    }
    private void interactWithNpc(Npc npc, String action) {
        if (Client.getLocalPlayer().getAnimationId() == -1) {
           // println(npc.getOptions());
            npc.interact(action);
           // println(action);
        }

        //println("Found new guard: " + npc.getName());
        botState = BotState.HARVEST_GUARD;
    }

    private void interactWithSceneObject(SceneObject sceneObject, String action) {
        if (Client.getLocalPlayer().getAnimationId() == -1) {
            println(sceneObject.getOptions());
            sceneObject.interact(action);
            println(action);
        }

        //println("Found new guard: " + sceneObject.getName());
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
//        Item results = InventoryItemQuery.newQuery(1464).ids(44550).results().first();
//
//        if (results != null) {
//            // Get the charges of the current Grace of the Elves
//            int charges = VarManager.getInvVarbit(results.getInventoryType().getId(), results.getSlot(), 30214);
//
//            // If the charges are 0, scan the backpack for another Grace of the Elves with charges greater than 0
//            if (charges == 0) {
//                println("Grace of the Elves charges are 0, scanning backpack for another Grace of the Elves with charges greater than 0");
//                for (Item item : Backpack.getItems()) {
//                    if (item.getId() == 44550) { // Check if the item is Grace of the Elves
//                        int itemCharges = VarManager.getInvVarbit(item.getInventoryType().getId(), item.getSlot(), 30214);
//                        if (itemCharges > 0) { // Check if the item has charges
//                            // Equip the item
//                            Backpack.interact(item.getName(), "Wear");
//                            Execution.delayUntil(2000, () -> VarManager.getInvVarbit(1464, 15, 30214) > 0);
//                            println("Equipped Grace of the Elves with charges: " + itemCharges);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
        if (actionTick >= 1) {
            actionTick = 0;
            return;
        }

        if (actionTick == 0 && useHunterCape && hunter) {
            activateHunterCape();
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

    public void activateHunterCape() {
        // Define the regex pattern to match all variations of the Hunter cape
        Pattern hunterCapePattern = Pattern.compile("Hunter cape( \\(t\\))?|Hooded hunter cape( \\(t\\))?");

        // Check the backpack
        for (Item item : Backpack.getItems()) {
            Matcher matcher = hunterCapePattern.matcher(item.getName());
            if (matcher.matches()) {
                ActionBar.useItem(item.getName(), "Activate");
                ScriptConsole.println("Activated Hunter Cape: " + item.getName());
                return;
            }
        }

        // Check the equipment cape slot
        Item equippedCape = Equipment.getItemIn(Equipment.Slot.CAPE);
        if (equippedCape != null) {
            Matcher matcher = hunterCapePattern.matcher(equippedCape.getName());
            if (matcher.matches()) {
                ActionBar.useItem(equippedCape.getName(), "Activate");
                ScriptConsole.println("Activated Hunter Cape: " + equippedCape.getName());
                return;
            }
        }

        ScriptConsole.println("No Hunter Cape found to activate.");
    }


    public BotState getBotState() {
        return botState;
    }

}
