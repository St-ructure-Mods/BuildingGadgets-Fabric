package com.direwolf20.buildinggadgets.common.tainted.inventory.handle;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;

public interface IObjectHandle {

    int match(UniqueItem item, int count, boolean simulate);

    int insert(UniqueItem item, int count, boolean simulate);

    boolean shouldCleanup();
}
