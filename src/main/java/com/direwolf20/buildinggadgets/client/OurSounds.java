package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class OurSounds {

    public static OurSounds BEEP;

    public static void initSounds() {
        BEEP = new OurSounds("beep");
    }

    private final SoundEvent sound;

    public OurSounds(String name) {
        ResourceLocation loc = new ResourceLocation(Reference.MODID, name);
        sound = new SoundEvent(loc);
        Registry.register(Registry.SOUND_EVENT, loc, sound);
    }

    public SoundEvent getSound() {
        return sound;
    }

    public void playSound() {
        playSound(1.0F);
    }

    public void playSound(float pitch) {
        BuildingGadgetsClient.playSound(sound, pitch);
    }
}
