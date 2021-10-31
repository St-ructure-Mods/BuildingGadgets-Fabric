package com.direwolf20.buildinggadgets.common.compat;

import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;

import java.util.ArrayList;
import java.util.List;


public class REICompat implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(TemplateManagerGUI.class, provider -> {
            List<Rectangle> rectangleList = new ArrayList<>();
            rectangleList.add(new Rectangle(370, 158, 178, 192));
            rectangleList.add(new Rectangle(545, 176, 77, 116));
            return rectangleList;
        });
    }
}
