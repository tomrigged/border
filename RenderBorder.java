package dev.tomax.renderborder;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.lwjgl.glfw.GLFW;

public class RenderBorder implements ClientModInitializer {
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        // Built-in texture pack (blue stripes). Off by default = normal vanilla border.
        FabricLoader.getInstance().getModContainer("renderborder").ifPresent(container ->
                ResourceLoader.registerBuiltinPack(
                        Identifier.fromNamespaceAndPath("renderborder", "strepen"),
                        container,
                        Component.literal("World border: blauwe strepen"),
                        PackActivationType.NORMAL));

        // Key (default B, changeable in Controls) and command /border switch between the two borders.
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("renderborder", "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.renderborder.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                toggle(client);
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("border").executes(ctx -> {
                    toggle(Minecraft.getInstance());
                    return 1;
                })));
    }

    private static void toggle(Minecraft mc) {
        PackRepository repo = mc.getResourcePackRepository();
        String id = null;
        for (Pack p : repo.getAvailablePacks()) {
            if (p.getId().contains("renderborder")) {
                id = p.getId();
            }
        }
        if (id == null) {
            say(mc, "Border-pack niet gevonden");
            return;
        }
        List<String> selected = new ArrayList<>(repo.getSelectedIds());
        boolean wasOn = selected.remove(id);
        if (!wasOn) {
            selected.add(id);
        }
        say(mc, wasOn ? "Border: normaal" : "Border: blauwe strepen");
        repo.setSelected(selected);
        mc.options.updateResourcePacks(repo);
    }

    private static void say(Minecraft mc, String text) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(text), true);
        }
    }
}
