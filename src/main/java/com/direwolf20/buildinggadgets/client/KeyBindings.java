package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final KeyMapping menuSettings = createBinding("settings_menu", GLFW.GLFW_KEY_G);
    public static final KeyMapping range = createBinding("range", GLFW.GLFW_KEY_R);
    public static final KeyMapping undo = createBinding("undo", GLFW.GLFW_KEY_U);
    public static final KeyMapping anchor = createBinding("anchor", GLFW.GLFW_KEY_H);
    public static final KeyMapping fuzzy = createBinding("fuzzy", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping connectedArea = createBinding("connected_area", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping rotateMirror = createBinding("rotate_mirror", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping materialList = createBinding("material_list", GLFW.GLFW_KEY_M);

    public static void initialize() {
        // Done in class init
    }

    private static KeyMapping createBinding(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(getKey(name), InputConstants.Type.KEYSYM, key, getKey("category")));
    }

    private static String getKey(String name) {
        return String.join(".", "key", Reference.MODID, name);
    }
}
