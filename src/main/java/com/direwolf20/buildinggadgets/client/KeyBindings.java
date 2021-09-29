package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyMapping menuSettings;
    public static KeyMapping range;
    public static KeyMapping rotateMirror;
    public static KeyMapping undo;
    public static KeyMapping anchor;
    public static KeyMapping fuzzy;
    public static KeyMapping connectedArea;
    public static KeyMapping materialList;

    public static void init() {
        menuSettings = createBinding("settings_menu", GLFW.GLFW_KEY_G);
        range = createBinding("range", GLFW.GLFW_KEY_R);
        undo = createBinding("undo", GLFW.GLFW_KEY_U);
        anchor = createBinding("anchor", GLFW.GLFW_KEY_H);
        fuzzy = createBinding("fuzzy", GLFW.GLFW_KEY_UNKNOWN);
        connectedArea = createBinding("connected_area", GLFW.GLFW_KEY_UNKNOWN);
        rotateMirror = createBinding("rotate_mirror", GLFW.GLFW_KEY_UNKNOWN);
        materialList = createBinding("material_list", GLFW.GLFW_KEY_M);
    }

    private static KeyMapping createBinding(String name, int key) {
        KeyMapping keyBinding = new KeyMapping(getKey(name), InputConstants.Type.KEYSYM, key, getKey("category"));
        KeyBindingHelper.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    private static String getKey(String name) {
        return String.join(".", "key", Reference.MODID, name);
    }
}
