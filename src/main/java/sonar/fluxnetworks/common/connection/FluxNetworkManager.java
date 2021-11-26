package sonar.fluxnetworks.common.connection;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.network.SecurityLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.UUID;

/**
 * Manages all logical flux networks and save their data to the save.
 * <p>
 * Only on logical server side.
 */
@NotThreadSafe
public final class FluxNetworkManager extends SavedData {

    private static final String NETWORK_DATA = FluxNetworks.MODID + "data";

    private static volatile FluxNetworkManager data;

    private static final String NETWORKS = "networks";
    //private static final String TICKETS = "tickets";
    private static final String UNIQUE_ID = "uniqueID";

    /*public static String NETWORK_PASSWORD = "networkPassword";
    public static String SECURITY_TYPE = "networkSecurity";
    public static String ENERGY_TYPE = "networkEnergy";
    public static String WIRELESS_MODE = "wirelessMode";*/

    /*public static String NETWORK_FOLDERS = "folders";
    public static String UNLOADED_CONNECTIONS = "unloaded";*/

    /*public static String OLD_NETWORK_ID = "id";
    public static String OLD_NETWORK_NAME = "name";
    public static String OLD_NETWORK_COLOR = "colour";
    public static String OLD_NETWORK_ACCESS = "access";*/

    private final Int2ObjectMap<IFluxNetwork> mNetworks = new Int2ObjectOpenHashMap<>();
    //private final Map<ResourceLocation, LongSet> tickets = new HashMap<>();

    private int mUniqueID = 1; // -1 for invalid

    private FluxNetworkManager() {
    }

    private FluxNetworkManager(@Nonnull CompoundTag tag) {
        read(tag);
    }

    @Nonnull
    private static FluxNetworkManager get() {
        if (data != null) {
            return data;
        }
        synchronized (FluxNetworkManager.class) {
            if (data == null) {
                ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
                data = level.getDataStorage()
                        .computeIfAbsent(FluxNetworkManager::new, FluxNetworkManager::new, NETWORK_DATA);
                FluxNetworks.LOGGER.info("FluxNetworkData has been successfully loaded");
            }
        }
        return data;
    }

    // called when the server instance changed, e.g. switching single player worlds (saves)
    public static void release() {
        if (data != null) {
            data = null;
            FluxNetworks.LOGGER.info("FluxNetworkData has been unloaded");
        }
    }

    @Nonnull
    public static IFluxNetwork getNetwork(int id) {
        return get().mNetworks.getOrDefault(id, FluxNetworkInvalid.INSTANCE);
    }

    @Nonnull
    public static Collection<IFluxNetwork> getAllNetworks() {
        return get().mNetworks.values();
    }

    /*
     * Get a set of block pos with given dimension key, a pos represents a flux tile entity
     * that wants to load the chunk it's in
     *
     * @param dim dimension
     * @return all block pos that want to load chunks they are in
     */
    /*@Nonnull
    public static LongSet getTickets(@Nonnull RegistryKey<World> dim) {
        return get().tickets.computeIfAbsent(dim.getLocation(), d -> new LongOpenHashSet());
    }*/

    @Nullable
    public static IFluxNetwork createNetwork(@Nonnull Player creator, @Nonnull String name, int color,
                                             @Nonnull SecurityLevel level, @Nonnull String password) {
        final FluxNetworkManager t = get();

        final boolean limitReached;
        if (FluxConfig.maximumPerPlayer == -1) {
            limitReached = false;
        } else {
            UUID uuid = creator.getUUID();
            long created = t.mNetworks.values().stream().filter(n -> n.getOwnerUUID().equals(uuid)).count();
            limitReached = created >= FluxConfig.maximumPerPlayer;
        }
        if (limitReached) {
            return null;
        }

        FluxNetworkServer network = new FluxNetworkServer(t.mUniqueID++, name, color, creator);
        network.getSecurity().set(level, password);

        if (t.mNetworks.put(network.getNetworkID(), network) != null) {
            FluxNetworks.LOGGER.warn("Network IDs are not unique when creating network");
        }
        //S2CNetMsg.updateNetwork(network, FluxConstants.TYPE_NET_BASIC).sendToAll();
        return network;
    }

    public static void deleteNetwork(@Nonnull IFluxNetwork network) {
        if (get().mNetworks.remove(network.getNetworkID()) == network) {
            network.onDelete();
        }
        //S2CNetMsg.updateNetwork(network, FluxConstants.TYPE_NET_DELETE).sendToAll();
    }

    @Override
    public boolean isDirty() {
        // always dirty as a convenience
        return true;
    }

    private void read(@Nonnull CompoundTag compound) {
        mUniqueID = compound.getInt(UNIQUE_ID);

        ListTag list = compound.getList(NETWORKS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            FluxNetworkServer network = new FluxNetworkServer();
            network.readCustomTag(tag, FluxConstants.TYPE_SAVE_ALL);
            if (mNetworks.put(network.getNetworkID(), network) != null) {
                FluxNetworks.LOGGER.warn("Network IDs are not unique when reading data");
            }
        }

        /*CompoundNBT tag = nbt.getCompound(TICKETS);
        for (String key : tag.keySet()) {
            ListNBT l2 = tag.getList(key, Constants.NBT.TAG_LONG);
            LongSet set = tickets.computeIfAbsent(new ResourceLocation(key), d -> new LongOpenHashSet());
            for (INBT n : l2) {
                try {
                    set.add(((LongNBT) n).getLong());
                } catch (RuntimeException ignored) {

                }
            }
        }
        data = this;*/
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        compound.putInt(UNIQUE_ID, mUniqueID);

        ListTag list = new ListTag();
        for (IFluxNetwork network : mNetworks.values()) {
            CompoundTag tag = new CompoundTag();
            network.writeCustomTag(tag, FluxConstants.TYPE_SAVE_ALL);
            list.add(tag);
        }
        compound.put(NETWORKS, list);

        /*CompoundNBT tag = new CompoundNBT();
        for (Map.Entry<ResourceLocation, LongSet> entry : tickets.entrySet()) {
            LongSet set = entry.getValue();
            if (!set.isEmpty()) {
                ListNBT l2 = new ListNBT();
                for (long l : set) {
                    l2.add(LongNBT.valueOf(l));
                }
                tag.put(entry.getKey().toString(), l2);
            }
        }
        compound.put(TICKETS, tag);*/
        return compound;
    }

    /*public static void readPlayers(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        if (!nbt.contains(FluxConstants.PLAYER_LIST)) {
            return;
        }
        List<NetworkMember> members = network.getMemberList();
        ListNBT list = nbt.getList(FluxConstants.PLAYER_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT c = list.getCompound(i);
            members.add(new NetworkMember(c));
        }
    }

    public static void writePlayers(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        List<NetworkMember> members = network.getMemberList();
        if (!members.isEmpty()) {
            ListNBT list = new ListNBT();
            members.forEach(s -> list.add(s.writeNBT(new CompoundNBT())));
            nbt.put(FluxConstants.PLAYER_LIST, list);
        }
    }

    public static void writeAllPlayers(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        List<NetworkMember> members = network.getMemberList();
        ListNBT list = new ListNBT();
        if (!members.isEmpty()) {
            members.forEach(s -> list.add(s.writeNBT(new CompoundNBT())));
        }
        List<ServerPlayerEntity> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
        if (!players.isEmpty()) {
            players.stream().filter(p -> members.stream().noneMatch(s -> s.getPlayerUUID().equals(p.getUniqueID())))
                    .forEach(s -> list.add(NetworkMember.create(s, getPermission(s)).writeNBT(new CompoundNBT())));
        }
        nbt.put(FluxConstants.PLAYER_LIST, list);
    }

    private static AccessLevel getPermission(@Nonnull PlayerEntity player) {
        return SuperAdmin.isPlayerSuperAdmin(player) ? AccessLevel.SUPER_ADMIN : AccessLevel.BLOCKED;
    }*/

    /*public static void readConnections(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        if (!nbt.contains(UNLOADED_CONNECTIONS)) {
            return;
        }
        List<IFluxDevice> a = network.getAllConnections();
        ListNBT list = nbt.getList(UNLOADED_CONNECTIONS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            a.add(new SimpleFluxDevice(list.getCompound(i)));
        }
    }

    public static void writeConnections(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        List<IFluxDevice> a = network.getAllConnections();
        if (!a.isEmpty()) {
            ListNBT list = new ListNBT();
            a.forEach(s -> {
                if (!s.isChunkLoaded()) {
                    list.add(s.writeCustomNBT(new CompoundNBT(), 0));
                }
            });
            nbt.put(UNLOADED_CONNECTIONS, list);
        }
    }

    public static void readAllConnections(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        if (!nbt.contains(UNLOADED_CONNECTIONS)) {
            return;
        }
        List<IFluxDevice> a = network.getAllConnections();
        ListNBT list = nbt.getList(UNLOADED_CONNECTIONS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            a.add(new SimpleFluxDevice(list.getCompound(i)));
        }
    }

    public static void writeAllConnections(IFluxNetwork network, @Nonnull CompoundNBT nbt) {
        List<IFluxDevice> a = network.getAllConnections();
        if (!a.isEmpty()) {
            ListNBT list = new ListNBT();
            a.forEach(s -> list.add(s.writeCustomNBT(new CompoundNBT(), NBTType.DEFAULT)));
            nbt.put(UNLOADED_CONNECTIONS, list);
        }
    }*/

    /*private void readChunks(CompoundNBT nbt) {
        if (!nbt.contains(LOADED_CHUNKS)) {
            return;
        }
        CompoundNBT tags = nbt.getCompound(LOADED_CHUNKS);
        for (String key : tags.keySet()) {
            ListNBT list = tags.getList(key, Constants.NBT.TAG_COMPOUND);
            List<ChunkPos> pos = forcedChunks.computeIfAbsent(Integer.valueOf(key), l -> new ArrayList<>());
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT tag = list.getCompound(i);
                pos.add(new ChunkPos(tag.getInt("x"), tag.getInt("z")));
            }
        }
    }*/

    /*private void writeChunks(int dim, List<ChunkPos> pos, CompoundNBT nbt) {
        if (!pos.isEmpty()) {
            ListNBT list = new ListNBT();
            pos.forEach(p -> {
                CompoundNBT t = new CompoundNBT();
                t.putInt("x", p.x);
                t.putInt("z", p.z);
                list.add(t);
            });
            nbt.put(String.valueOf(dim), list);
        }
    }*/

    /*private static void readOldData(FluxNetworkBase network, CompoundNBT nbt) {
        network.network_id.setValue(nbt.getInt(FluxNetworkData.OLD_NETWORK_ID));
        network.network_name.setValue(nbt.getString(FluxNetworkData.OLD_NETWORK_NAME));
        CompoundNBT color = nbt.getCompound(FluxNetworkData.OLD_NETWORK_COLOR);
        network.network_color.setValue(color.getInt("red") << 16 | color.getInt("green") << 8 | color.getInt("blue"));
        network.network_owner.setValue(nbt.getUniqueId(FluxNetworkData.OWNER_UUID));
        int c = nbt.getInt(FluxNetworkData.OLD_NETWORK_ACCESS);
        network.network_security.setValue(c > 0 ? EnumSecurityType.ENCRYPTED : EnumSecurityType.PUBLIC);
        network.network_password.setValue(String.valueOf((int) (Math.random() * 1000000)));
        network.network_energy.setValue(EnergyType.FE);
        FluxNetworkData.readPlayers(network, nbt);
        FluxNetworkData.readConnections(network, nbt);
    }*/
}
