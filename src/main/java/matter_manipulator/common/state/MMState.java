package matter_manipulator.common.state;

import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.items.MMUpgrades;
import matter_manipulator.common.items.ManipulatorTier;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.context.StackManipulatorContext;
import matter_manipulator.core.manipulator_resource.ManipulatorResource;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.DataStorage;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.persist.NBTPersist;

/// The NBT state of a manipulator.
@SuppressWarnings("unused")
public class MMState {

    @SerializedName("jv")
    private int jsonVersion = LASTEST_JSON_VERSION;
    @SerializedName("dv")
    private int dataVersion = LASTEST_DATA_VERSION;

    public ResourceLocation activeMode = null;
    public Object2ObjectOpenHashMap<ResourceLocation, DataStorage> modeState = new Object2ObjectOpenHashMap<>();

    public DataStorage settings = new DataStorage();

    @Getter
    @Nullable
    public Transform transform;

    public BitSet installedUpgrades = new BitSet();
    public transient int upgradeProvidedCapabilities;

    public transient ItemMatterManipulator manipulator;

    public DataStorage resources = new DataStorage();

    public DataStorage ioState = new DataStorage();

    public transient Map<ResourceLocation, ManipulatorResource> resourceMap;

    public transient Runnable save;

    public static MMState load(NBTTagCompound tag) {
        JsonObject obj = (JsonObject) NBTPersist.toJsonObject(tag);

        migrateJson(obj);

        MMState state = NBTPersist.GSON.fromJson(obj, MMState.class);

        if (state == null) state = new MMState();

        state.migrate();
        state.onLoad();

        return state;
    }

    public NBTTagCompound save() {
        return (NBTTagCompound) NBTPersist.toNbt(NBTPersist.GSON.toJsonTree(this));
    }

    private static final int LASTEST_JSON_VERSION = 0;
    private static final int LASTEST_DATA_VERSION = 0;

    private static void migrateJson(JsonObject obj) {
        int version = obj.has("jv") ? obj.get("jv").getAsInt() : LASTEST_JSON_VERSION;

        obj.addProperty("jv", version);
    }

    private void migrate() {

    }

    private void onLoad() {
        for (MMUpgrades upgrade : getInstalledUpgrades()) {
            upgradeProvidedCapabilities |= upgrade.providesCaps;
        }
    }

    public void setSaveDelegate(Runnable save) {
        this.save = save;

        modeState.values().forEach(d -> d.save = save);
        settings.save = save;
        resources.save = save;
        ioState.save = save;
    }

    public Map<ResourceLocation, ManipulatorResource> getResources(StackManipulatorContext context) {
        if (resourceMap != null) return this.resourceMap;

        this.resourceMap = new Object2ObjectOpenHashMap<>();

        for (var entry : MMRegistriesInternal.RESOURCE_LOADERS.entrySet()) {
            var resource = entry.getValue().load(context, this.resources);

            resource.ifPresent(manipulatorResource -> this.resourceMap.put(entry.getKey(), manipulatorResource));
        }

        return this.resourceMap;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public ManipulatorMode getActiveMode() {
        return MMRegistriesInternal.MODES.get(activeMode);
    }

    public IDataStorage getActiveModeConfigStorage() {
        if (this.activeMode == null) return null;

        DataStorage storage = this.modeState.get(this.activeMode);

        if (storage == null) {
            storage = new DataStorage();
            storage.save = save;
            this.modeState.put(this.activeMode, storage);
        }

        return storage;
    }

    @SuppressWarnings("rawtypes")
    public List<ManipulatorMode> getPossibleModes() {
        return MMRegistriesInternal.MODES.entrySet()
            .stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .map(Entry::getValue)
            .collect(Collectors.toList());
    }

    public boolean hasCap(int cap) {
        return (manipulator.tier.capabilities & cap) == cap || (upgradeProvidedCapabilities & cap) == cap;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public MMState clone() {
        MMState copy = load(save());

        copy.manipulator = this.manipulator;

        return copy;
    }

    public boolean hasUpgrade(MMUpgrades upgrade) {
        return upgrade != null && installedUpgrades.get(upgrade.bit);
    }

    public boolean installUpgrade(MMUpgrades upgrade) {
        if (installedUpgrades.get(upgrade.bit)) return false;

        installedUpgrades.set(upgrade.bit);

        return true;
    }

    public boolean couldAcceptUpgrade(ManipulatorTier tier, MMUpgrades upgrade) {
        return tier.allowedUpgrades.contains(upgrade) && !installedUpgrades.get(upgrade.bit);
    }

    public Set<MMUpgrades> getInstalledUpgrades() {
        ObjectOpenHashSet<MMUpgrades> set = new ObjectOpenHashSet<>();

        for (MMUpgrades upgrade : MMUpgrades.values()) {
            if (hasUpgrade(upgrade)) set.add(upgrade);
        }

        return set;
    }
}
