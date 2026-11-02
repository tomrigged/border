package dev.tomax.renderborder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RenderBorder implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Built-in pack: shows up in Options > Resource Packs as "World border: blauwe strepen".
        // Off = normal vanilla world border. On = blue stripes. Off by default.
        FabricLoader.getInstance().getModContainer("renderborder").ifPresent(container ->
                ResourceLoader.registerBuiltinPack(
                        Identifier.fromNamespaceAndPath("renderborder", "strepen"),
                        container,
                        Component.literal("World border: blauwe strepen"),
                        PackActivationType.NORMAL));
    }
}
