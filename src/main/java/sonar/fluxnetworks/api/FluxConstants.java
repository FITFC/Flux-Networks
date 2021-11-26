package sonar.fluxnetworks.api;

/**
 * Revision: 6.0.0
 */
public class FluxConstants {

    public static final int INVALID_NETWORK_ID = -1;
    public static final int INVALID_NETWORK_COLOR = 0xb2b2b2;

    // NBT access type, store data on server disk
    public static final int TYPE_SAVE_ALL = 1;

    // NBT access type, tile update or read stack
    public static final int TYPE_TILE_UPDATE = 11;
    public static final int TYPE_TILE_DROP = 15;

    // NBT access type, update phantom flux device
    public static final int TYPE_CONNECTION_UPDATE = 20;

    // NBT access type, network data-sync or operation
    public static final int TYPE_NET_BASIC = 21;
    public static final int TYPE_NET_MEMBERS = 22;
    public static final int TYPE_NET_CONNECTIONS = 23;
    public static final int TYPE_NET_STATISTICS = 24;
    public static final int TYPE_NET_DELETE = 29;

    // Network connections editing flags
    public static final int FLAG_EDIT_NAME = 1;
    public static final int FLAG_EDIT_PRIORITY = 1 << 1;
    public static final int FLAG_EDIT_LIMIT = 1 << 2;
    public static final int FLAG_EDIT_SURGE_MODE = 1 << 3;
    public static final int FLAG_EDIT_DISABLE_LIMIT = 1 << 4;
    public static final int FLAG_EDIT_CHUNK_LOADING = 1 << 5;
    public static final int FLAG_EDIT_DISCONNECT = 1 << 6;

    // Network members editing type
    public static final int TYPE_NEW_MEMBER = 1;
    public static final int TYPE_SET_ADMIN = 2;
    public static final int TYPE_SET_USER = 3;
    public static final int TYPE_CANCEL_MEMBERSHIP = 4;
    public static final int TYPE_TRANSFER_OWNERSHIP = 5;

    // Tile message type
    public static final byte C2S_CUSTOM_NAME = 1;
    public static final byte C2S_PRIORITY = 2;
    public static final byte C2S_LIMIT = 3;
    public static final byte C2S_SURGE_MODE = 4;
    public static final byte C2S_DISABLE_LIMIT = 5;
    public static final byte C2S_CHUNK_LOADING = 6;

    public static final byte S2C_GUI_SYNC = -1;
    public static final byte S2C_STORAGE_ENERGY = -2;

    // NBT subtag key
    public static final String TAG_FLUX_DATA = "FluxData";
    public static final String TAG_FLUX_CONFIG = "FluxConfig";

    // NBT root key
    public static final String FLUX_COLOR = "FluxColor";

    // NBT key
    public static final String NETWORK_ID = "networkID";
    public static final String CUSTOM_NAME = "customName";
    public static final String PRIORITY = "priority";
    public static final String LIMIT = "limit";
    public static final String SURGE_MODE = "surgeMode";
    public static final String DISABLE_LIMIT = "disableLimit";
    public static final String PLAYER_UUID = "playerUUID";

    public static final String CLIENT_COLOR = "clientColor";
    public static final String FLAGS = "flags";

    public static final String DEVICE_TYPE = "deviceType";
    public static final String FORCED_LOADING = "forcedLoading";
    public static final String CHUNK_LOADED = "chunkLoaded";

    public static final String BUFFER = "buffer";
    public static final String ENERGY = "energy"; // equals to buffer, but with different display text
    public static final String CHANGE = "change";
}
