package com.direwolf20.buildinggadgets.common.tainted.template;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;

import java.util.UUID;
import java.util.function.Supplier;

public interface ITemplateKey extends Component {
    UUID getTemplateId(Supplier<UUID> freeIdAllocator);
}
