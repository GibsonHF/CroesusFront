package me.gibson;

import net.botwithus.rs3.game.login.LoginManager;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class GraphicsContext extends ScriptGraphicsContext {

    private CroesusFrontScript script;
    public int HunterXPGained = 0;
    public int XPStart = 0;

    public GraphicsContext(ScriptConsole scriptConsole, CroesusFrontScript script) {
        super(scriptConsole);
        this.script = script;
        this.XPStart = Skills.HUNTER.getSkill().getExperience();

    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("Croesus Front Script", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("Hunter Bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    script.runScript = ImGui.Checkbox("Run Script", script.runScript);
                    script.Hopping = ImGui.Checkbox("Hopping", script.Hopping);
                    script.useBikBook = ImGui.Checkbox("Use Bik Book", script.useBikBook);
                    if (ImGui.Checkbox("Hunter", script.hunter)) {
                        handleCheckboxSelection("Hunter");
                    }
                    if (ImGui.Checkbox("Mining", script.mining)) {
                        handleCheckboxSelection("Mining");
                    }
                    if (ImGui.Checkbox("Woodcutting", script.woodcutting)) {
                        handleCheckboxSelection("Woodcutting");
                    }
                    if (ImGui.Checkbox("Fishing", script.fishing)) {
                        handleCheckboxSelection("Fishing");
                    }
                    if(script.hunter)
                    {
                        script.useHunterCape = ImGui.Checkbox("Use Hunter Cape", script.useHunterCape);
                        if (ImGui.Button("Reset XP gained")) {
                            HunterXPGained = 0;
                            script.xpGained = 0;
                            XPStart = Skills.HUNTER.getSkill().getExperience();
                        }
                        ImGui.Text("XP Gained: " + calculateConstructionXPGained() + " XP");

                    }
                    ImGui.Text("My scripts state is: " + script.getBotState());
                    ImGui.Text("Script Runtime: " + script.getRunTime());
                    ImGui.Text("Average XP per hour: " + calculateAverageXPPerHour() + " XP");
                    ImGui.Text("Times Hopped: " + script.timesHopped);
                    ImGui.Text("TTL: " + calculateTimeTillLevel());
                    int currentXp = Skills.HUNTER.getSkill().getExperience();
                    int currentLevel = ExperienceTable.getLevelForExperience(currentXp);
                    int xpForCurrentLevel = ExperienceTable.getExperienceForLevel(currentLevel);
                    int xpForNextLevel = ExperienceTable.getExperienceForNextLevel(currentXp);

                    float progress = (float) (currentXp - xpForCurrentLevel) / (xpForNextLevel - xpForCurrentLevel);

                    String progressPercentage = String.format("%.2f", progress * 100);

                    String progressBarLabel = "Progress to level " + (currentLevel + 1) + ": " + progressPercentage + "%";

                    ImGui.ProgressBar(progressBarLabel, progress, 0.0f, 0.0f);
                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

    }

    public String calculateTimeTillLevel() {
        int currentXp = Skills.HUNTER.getSkill().getExperience();
        int currentLevel = ExperienceTable.getLevelForExperience(currentXp);
        int xpForNextLevel = ExperienceTable.getExperienceForNextLevel(currentXp);

        // Calculate the remaining XP to the next level
        int remainingXp = xpForNextLevel - currentXp;

        // Calculate the average XP per hour
        double xpPerHour = Double.parseDouble(calculateAverageXPPerHour().replace(",", ""));

        // Calculate the time in hours
        double timeInHours = remainingXp / xpPerHour;

        // Convert the time in hours to hours, minutes, and seconds
        int hours = (int) timeInHours;
        int minutes = (int) ((timeInHours - hours) * 60);
        int seconds = (int) (((timeInHours - hours) * 60 - minutes) * 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String calculateConstructionXPGained() {
        // Calculate the Construction XP gained
        HunterXPGained = Skills.HUNTER.getSkill().getExperience() - XPStart;

        // Format the XP gained with commas for thousands and a decimal point
        NumberFormat numberFormat = NumberFormat.getInstance();
        String formattedXpGained = numberFormat.format(HunterXPGained);

        return formattedXpGained;
    }

    public String calculateAverageXPPerHour() {
        // Calculate the average XP per hour
        long runTimeInMilliseconds = System.currentTimeMillis() - script.startTime;
        double runTimeInHours = runTimeInMilliseconds / 1000.0 / 60.0 / 60.0; // Convert runtime from milliseconds to hours
        double xpPerHour = (double) HunterXPGained / runTimeInHours;

        // Format the XP per hour with commas for thousands and a decimal point
        NumberFormat numberFormat = NumberFormat.getInstance();
        String formattedXpPerHour = numberFormat.format((int) xpPerHour);


        return formattedXpPerHour;
    }

    public void handleCheckboxSelection(String checkboxName) {
        script.hunter = checkboxName.equals("Hunter");
        script.mining = checkboxName.equals("Mining");
        script.woodcutting = checkboxName.equals("Woodcutting");
        script.fishing = checkboxName.equals("Fishing");
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
    public class ExperienceTable {
        private static final Map<Integer, Integer> experienceForLevel = new HashMap<>();

        static {
            experienceForLevel.put(100, 14391160);
            experienceForLevel.put(101, 15889109);
            experienceForLevel.put(102, 17542976);
            experienceForLevel.put(103, 19368992);
            experienceForLevel.put(104, 21385073);
            experienceForLevel.put(105, 23611006);
            experienceForLevel.put(106, 26068632);
            experienceForLevel.put(107, 28782069);
            experienceForLevel.put(108, 31777943);
            experienceForLevel.put(109, 35085654);
            experienceForLevel.put(110, 38737661);
            experienceForLevel.put(111, 42769801);
            experienceForLevel.put(112, 47221641);
            experienceForLevel.put(113, 52136869);
            experienceForLevel.put(114, 57563718);
            experienceForLevel.put(115, 63555443);
            experienceForLevel.put(116, 70170840);
            experienceForLevel.put(117, 77474828);
            experienceForLevel.put(118, 85539082);
            experienceForLevel.put(119, 94442737);
            experienceForLevel.put(120, 104273167);
        }

        public static int getLevelForExperience(int experience) {
            int level = 0;
            for (Map.Entry<Integer, Integer> entry : experienceForLevel.entrySet()) {
                if (experience < entry.getValue()) {
                    break;
                }
                level = entry.getKey();
            }
            return level;
        }

        public static int getExperienceForNextLevel(int experience) {
            int level = getLevelForExperience(experience);
            return experienceForLevel.get(level + 1);
        }

        public static int getExperienceForLevel(int level) {
            return experienceForLevel.getOrDefault(level, 0);
        }
    }
}

