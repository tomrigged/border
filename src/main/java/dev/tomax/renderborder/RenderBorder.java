package dev.tomax.renderborder;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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
    private static final String MOD = "renderborder";
    /** Available borders. "normaal" = no pack = the normal vanilla border. */
    static final String[] NAMES = {"normaal", "strepen", "banden", "raster", "stippen", "effen"};
    private static KeyMapping toggleKey;
    private static boolean openMenu;

    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer(MOD).ifPresent(container -> {
            for (String n : NAMES) {
                if (n.equals("normaal")) continue;
                ResourceLoader.registerBuiltinPack(
                        Identifier.fromNamespaceAndPath(MOD, n),
                        container,
                        Component.literal("World border: " + n),
                        PackActivationType.NORMAL);
            }
        });

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD, "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.renderborder.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                openMenu = true;
            }
            if (openMenu && client.player != null) {
                openMenu = false;
                client.setScreen(new BorderScreen(null));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("border")
                    .executes(ctx -> {
                        openMenu = true;
                        return 1;
                    });
            for (String n : NAMES) {
                root.then(ClientCommandManager.literal(n).executes(ctx -> {
                    select(Minecraft.getInstance(), n);
                    return 1;
                }));
            }
            dispatcher.register(root);
        });
    }

    private static boolean ours(String packId) {
        return packId.startsWith(MOD + ":");
    }

    static String current(Minecraft mc) {
        for (String id : mc.getResourcePackRepository().getSelectedIds()) {
            if (ours(id)) return id.substring(MOD.length() + 1);
        }
        return "normaal";
    }

    static void select(Minecraft mc, String name) {
        PackRepository repo = mc.getResourcePackRepository();
        List<String> selected = new ArrayList<>();
        for (String id : repo.getSelectedIds()) {
            if (!ours(id)) selected.add(id);
        }
        if (!name.equals("normaal")) {
            String wanted = MOD + ":" + name;
            boolean found = false;
            for (Pack p : repo.getAvailablePacks()) {
                if (p.getId().equals(wanted)) found = true;
            }
            if (!found) {
                say(mc, "Border-pack '" + name + "' niet gevonden (id " + wanted + ")");
                return;
            }
            selected.add(wanted);
        }
        say(mc, "Border: " + name);
        repo.setSelected(selected);
        mc.options.updateResourcePacks(repo);
    }

    private static void say(Minecraft mc, String text) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(text), false);
        }
    }
}
