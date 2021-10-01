package com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects;

import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.direwolf20.buildinggadgets.common.util.ref.JsonKeys;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An {@link UniqueItem} which represents all defining (unique) properties of an ItemStack: the item and the 2 types of nbt.
 * For convenience 2 match types are provided for the nbt data.
 */
public final class UniqueItem {
    public enum ComparisonMode {
        EXACT_MATCH(0) {
            @Override
            public boolean match(CompoundTag nbt, @Nullable CompoundTag other) {
                return nbt.equals(other);
            }
        },
        SUB_TAG_MATCH(1) {
            @Override
            public boolean match(CompoundTag nbt, @Nullable CompoundTag other) {
                if (other == null) {
                    return false;
                }
                for (String key : nbt.getAllKeys()) {
                    Tag val = nbt.get(key);
                    if (val == null) {
                        if (other.get(key) != null) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                    if (!other.contains(key, val.getId()) || other.get(key) == null) {
                        return false;
                    }
                    if (val.getId() == NbtType.COMPOUND && !match((CompoundTag) val, other.getCompound(key))) {
                        return false;
                    } else if (val.getId() != NbtType.COMPOUND && !val.getAsString().equals(other.get(key).getAsString())) {
                        return false;
                    }
                }
                return true;
            }
        };
        private static final Byte2ObjectMap<ComparisonMode> BY_ID = new Byte2ObjectOpenHashMap<>();
        private final byte id;

        ComparisonMode(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return id;
        }

        public abstract boolean match(CompoundTag nbt, @Nullable CompoundTag other);

        public static ComparisonMode byId(byte id) {
            ComparisonMode mode = BY_ID.get(id);
            return mode == null ? EXACT_MATCH : mode;//prevent future additions from crashing older clients...
        }

        static {
            Arrays.stream(values()).forEach(m -> BY_ID.put(m.getId(), m));
        }
    }

    public static UniqueItem ofStack(ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        stack.save(nbt);
        return new UniqueItem(stack.getItem(), stack.getTag(), ComparisonMode.EXACT_MATCH, nbt.getCompound("ForgeCaps"), ComparisonMode.EXACT_MATCH);
    }

    private final Item item;
    @Nullable
    private final CompoundTag tagCompound;
    private final int hash;
    private final ComparisonMode tagMatch;
    private final ComparisonMode capMatch;

    public UniqueItem(Item item) {
        this(item, null, ComparisonMode.EXACT_MATCH);
    }

    public UniqueItem(Item item, @Nullable CompoundTag tagCompound, ComparisonMode comparisonMode) {
        this(item, tagCompound, comparisonMode, null, ComparisonMode.EXACT_MATCH);
    }

    public UniqueItem(Item item, @Nullable CompoundTag tagCompound, ComparisonMode tagMatch, @Nullable CompoundTag forgeCaps, ComparisonMode capMatch) {
        this.item = Objects.requireNonNull(item, "Cannot construct a UniqueItem for a null Item!");
        this.tagCompound = tagCompound;
        this.tagMatch = Objects.requireNonNull(tagMatch);
        this.capMatch = Objects.requireNonNull(capMatch);
        int hash = capMatch.hashCode() + 31 * tagMatch.hashCode();
        hash = tagCompound != null ? tagCompound.hashCode() + 31 * hash : hash;
        hash = forgeCaps != null ? forgeCaps.hashCode() + 31 * hash : hash;
        this.hash = Registry.ITEM.getKey(item).hashCode() + 31 * hash;
    }

    public Class<Item> getIndexClass() {
        return Item.class;
    }

    public Item getIndexObject() {
        return item;
    }

    @Nullable
    public CompoundTag getTag() {
        return tagCompound != null ? tagCompound.copy() : null;
    }

    public ItemStack createStack() {
        return createStack(1);
    }

    public ItemStack createStack(int count) {
        ItemStack res = new ItemStack(item, count);
        res.setTag(tagCompound);
        return res;
    }

    public boolean matches(ItemStack stack) {
        if (stack.getItem() != getIndexObject()) {
            return false;
        }
        return tagCompound == null || tagMatch.match(tagCompound, stack.getTag());
    }

    public ItemStack insertInto(ItemStack stack, int count) {
        stack.setCount(Math.min(stack.getCount() + count, stack.getMaxStackSize()));
        if (tagCompound != null) {
            stack.setTag(tagCompound);
        }
        return stack;
    }

    public IUniqueObjectSerializer getSerializer() {
        return SerialisationSupport.uniqueItemSerializer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UniqueItem)) {
            return false;
        }
        UniqueItem that = (UniqueItem) o;
        if (!item.equals(that.item)) {
            return false;
        }
        if (tagCompound != null ? !tagCompound.equals(that.tagCompound) : that.tagCompound != null) {
            return false;
        }
        if (tagMatch != that.tagMatch) {
            return false;
        }
        return capMatch == that.capMatch;
    }

    private ResourceLocation getObjectRegistryName() {
        return Registry.ITEM.getKey(getIndexObject());
    }

    public Optional<ItemStack> tryCreateInsertStack(Map<Class<?>, Map<Object, List<IObjectHandle>>> index, int count) {
        return Optional.of(createStack(count));
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("item", getObjectRegistryName())
                .add("tagCompound", tagCompound)
                .add("tagMatch", tagMatch)
                .add("capMatch", capMatch)
                .toString();
    }

    public static final class Serializer implements IUniqueObjectSerializer {
        @Override
        public CompoundTag serialize(UniqueItem obj, boolean persisted) {
            UniqueItem item = (UniqueItem) obj;
            CompoundTag res = new CompoundTag();
            if (item.tagCompound != null) {
                res.put(NBTKeys.KEY_DATA, item.tagCompound);
            }
            res.putString(NBTKeys.KEY_ID, item.getObjectRegistryName().toString());
            res.putByte(NBTKeys.KEY_DATA_COMPARISON, item.tagMatch.getId());
            res.putByte(NBTKeys.KEY_CAP_COMPARISON, item.capMatch.getId());
            return res;
        }

        @Override
        public UniqueItem deserialize(CompoundTag res) {
            Preconditions.checkArgument(res.contains(NBTKeys.KEY_ID), "Cannot construct a UniqueItem without an Item!");
            CompoundTag nbt = res.getCompound(NBTKeys.KEY_DATA);
            ComparisonMode mode = ComparisonMode.byId(res.getByte(NBTKeys.KEY_DATA_COMPARISON));
            CompoundTag capNbt = res.getCompound(NBTKeys.KEY_CAP_NBT);
            ComparisonMode capMode = ComparisonMode.byId(res.getByte(NBTKeys.KEY_CAP_COMPARISON));
            Item item = Registry.ITEM.get(new ResourceLocation(res.getString(NBTKeys.KEY_ID)));
            return new UniqueItem(item, nbt.isEmpty() ? null : nbt, mode, capNbt.isEmpty() ? null : capNbt, capMode);
        }

        @Override
        public JsonSerializer<UniqueItem> asJsonSerializer(boolean printName, boolean extended) {
            return (uobj, typeOfSrc, context) -> {
                JsonObject obj = new JsonObject();
                UniqueItem element = (UniqueItem) uobj;
                Item item = element.getIndexObject();
                if (printName) {
                    obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_NAME, I18n.get(item.getDescriptionId(element.createStack())));
                }
                obj.add(JsonKeys.MATERIAL_LIST_ITEM_ID, context.serialize(element.getObjectRegistryName()));
                if (extended && element.tagCompound != null && !element.tagCompound.isEmpty()) {
                    obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_NBT, element.tagCompound.toString());
                    obj.add(JsonKeys.MATERIAL_LIST_ITEM_NBT_MATCH, context.serialize(element.tagMatch));
                }
                return obj;
            };
        }

        @Override
        public JsonDeserializer<UniqueItem> asJsonDeserializer() {
            return (json, typeOfT, context) -> {
                JsonObject object = json.getAsJsonObject();
                ResourceLocation registryName = context.deserialize(object.get(JsonKeys.MATERIAL_LIST_ITEM_ID), ResourceLocation.class);
                Item item = Registry.ITEM.get(registryName);
                CompoundTag tagCompound = null;
                ComparisonMode tagMatch = ComparisonMode.EXACT_MATCH;
                if (object.has(JsonKeys.MATERIAL_LIST_ITEM_NBT)) {
                    try {
                        tagCompound = TagParser.parseTag(object.getAsJsonPrimitive(JsonKeys.MATERIAL_LIST_ITEM_NBT).getAsString());
                        tagMatch = context.deserialize(object.get(JsonKeys.MATERIAL_LIST_ITEM_NBT_MATCH), ComparisonMode.class);
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                CompoundTag forgeCaps = null;
                ComparisonMode capMatch = ComparisonMode.EXACT_MATCH;
                if (object.has(JsonKeys.MATERIAL_LIST_CAP_NBT)) {
                    try {
                        forgeCaps = TagParser.parseTag(object.getAsJsonPrimitive(JsonKeys.MATERIAL_LIST_CAP_NBT).getAsString());
                        capMatch = context.deserialize(object.get(JsonKeys.MATERIAL_LIST_CAP_NBT_MATCH), ComparisonMode.class);
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return new UniqueItem(item, tagCompound, tagMatch, forgeCaps, capMatch);
            };
        }
    }
}
