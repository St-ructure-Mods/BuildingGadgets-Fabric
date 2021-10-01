package com.direwolf20.buildinggadgets.common.tainted.inventory.handle;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;

public interface IObjectHandle {

    int match(IUniqueObject item, int count, boolean simulate);

    int insert(IUniqueObject item, int count, boolean simulate);

    boolean shouldCleanup();
}
