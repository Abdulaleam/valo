package rainy.valo.Agents;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgentStorage extends PersistentState {
    private static final String KEY = "valo_agents";
    public static final PersistentState.Type<AgentStorage> TYPE =
            new PersistentState.Type<>(AgentStorage::new, AgentStorage::fromNbt, null);

    private final Map<UUID, String> selected = new HashMap<>();

    public static AgentStorage get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, KEY);
    }

    public static AgentStorage fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        AgentStorage storage = new AgentStorage();
        NbtCompound map = nbt.getCompound("selected");
        for (String key : map.getKeys()) {
            try {
                storage.selected.put(UUID.fromString(key), map.getString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return storage;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound map = new NbtCompound();
        selected.forEach(((uuid, id) -> map.putString(uuid.toString(), id)));
        nbt.put("selected", map);
        return nbt;
    }

    public String get(UUID player) {
        return selected.get(player);
    }
    public void set(UUID player, String agentId) {
        selected.put(player, agentId);
        markDirty();
    }

    public void remove(UUID player) {
        if (selected.remove(player) != null)
            markDirty();
    }
}