package me.gibson;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.text.NumberFormat;

public class GraphicsContext extends ScriptGraphicsContext {

    private CroesusFrontScript script;
    public int HunterXPGained = 0;
    public int XPStart;

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

                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

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
        int xpPerHour = (int) (HunterXPGained / runTimeInHours);

        // Format the XP per hour with commas for thousands and a decimal point
        NumberFormat numberFormat = NumberFormat.getInstance();
        String formattedXpPerHour = numberFormat.format(xpPerHour);

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
}
