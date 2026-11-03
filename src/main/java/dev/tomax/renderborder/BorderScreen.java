package dev.tomax.renderborder;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Simple menu: one button per border. */
public class BorderScreen extends Screen {
    private final Screen parent;

    public BorderScreen(Screen parent) {
        super(Component.literal("Kies je world border"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        String cur = RenderBorder.current(this.minecraft);
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - (RenderBorder.NAMES.length * 24 + 30) / 2;
        for (String n : RenderBorder.NAMES) {
            String label = n.equals(cur) ? "> " + n + " <" : n;
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                RenderBorder.select(this.minecraft, n);
                this.rebuildWidgets();
            }).bounds(x, y, w, h).build());
            y += 24;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Klaar"), b -> this.onClose())
                .bounds(x, y + 6, w, h).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
