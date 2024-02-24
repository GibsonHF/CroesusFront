package net.botwithus;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.text.NumberFormat;

public class GraphicsContext extends ScriptGraphicsContext {

    private HunterScript script;
    public int ConstructionXPGained = 0;
    public int XPStart;

    public GraphicsContext(ScriptConsole scriptConsole, HunterScript script) {
        super(scriptConsole);
        this.script = script;
        this.XPStart = Skills.HUNTER.getSkill().getExperience();

    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("Hunter Script", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("Hunter Bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    script.runScript = ImGui.Checkbox("Run Script", script.runScript);
                    ImGui.Text("My scripts state is: " + script.getBotState());
                    if(ImGui.Button("Reset XP gained")) {
                        ConstructionXPGained = 0;
                        script.xpGained = 0;
                        XPStart = Skills.HUNTER.getSkill().getExperience();
                    }
                    ImGui.Text("XP Gained: " + calculateConstructionXPGained() + " XP");

                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

    }

    public String calculateConstructionXPGained() {
        // Calculate the Construction XP gained
        ConstructionXPGained = Skills.CONSTRUCTION.getSkill().getExperience() - XPStart;

        // Format the XP gained with commas for thousands and a decimal point
        NumberFormat numberFormat = NumberFormat.getInstance();
        String formattedXpGained = numberFormat.format(ConstructionXPGained);

        return formattedXpGained;
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
