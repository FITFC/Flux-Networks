package sonar.fluxnetworks.common.misc;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.LazyOptional;
import sonar.fluxnetworks.api.device.IFluxDevice;
import sonar.fluxnetworks.api.misc.EnergyType;
import sonar.fluxnetworks.api.network.FluxDeviceType;
import sonar.fluxnetworks.api.text.FluxTranslate;
import sonar.fluxnetworks.common.tileentity.TileFluxDevice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class FluxUtils {

    private static final double[] COMPACT_SCALE = new double[]{0.001D, 0.000_001D, 0.000_000_001D, 0.000_000_000_001D,
            0.000_000_000_000_001D, 0.000_000_000_000_000_001D};

    private FluxUtils() {
    }

    @Nonnull
    public static <E extends Enum<?>> E incrementEnum(@Nonnull E val, @Nonnull E[] values) {
        int next = val.ordinal() + 1;
        if (next < values.length) {
            return values[next];
        }
        return values[0];
    }

    @Nullable
    public static Direction getBlockDirection(BlockPos pos, BlockPos other) {
        if (pos == other)
            return null;
        for (Direction face : Direction.values())
            if (pos.offset(face).equals(other))
                return face;
        return null;
    }

    @Nonnull
    public static String getTransferInfo(@Nonnull IFluxDevice flux, EnergyType energyType) {
        FluxDeviceType type = flux.getDeviceType();
        long change = flux.getTransferChange();
        if (type.isPlug()) {
            if (change == 0) {
                return FluxTranslate.INPUT.t() + ": " + TextFormatting.GOLD + "0 " + energyType.getUsageSuffix();
            } else {
                return FluxTranslate.INPUT.t() + ": " + TextFormatting.GREEN + "+" + EnergyType.usage(change);
            }
        }
        if (type.isPoint() || type.isController()) {
            if (change == 0) {
                return FluxTranslate.OUTPUT.t() + ": " + TextFormatting.GOLD + "0 " + energyType.getUsageSuffix();
            } else {
                return FluxTranslate.OUTPUT.t() + ": " + TextFormatting.RED + EnergyType.usage(change);
            }
        }
        if (type.isStorage()) {
            if (change == 0) {
                return FluxTranslate.CHANGE.t() + ": " + TextFormatting.GOLD + "0 " + energyType.getUsageSuffix();
            } else if (change > 0) {
                return FluxTranslate.CHANGE.t() + ": " + TextFormatting.GREEN + "+" + EnergyType.usage(change);
            } else {
                return FluxTranslate.CHANGE.t() + ": " + TextFormatting.RED + EnergyType.usage(change);
            }
        }
        return "";
    }

    /*public static int getPlayerXP(EntityPlayer player) {
        return (int) (getExperienceForLevel(player.experienceLevel) + (player.experience * player.xpBarCap()));
    }

    public static void addPlayerXP(EntityPlayer player, int amount) {
        int experience = getPlayerXP(player) + amount;
        player.experienceTotal = experience;
        player.experienceLevel = getLevelForExperience(experience);
        int expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experience = (float) (experience - expForLevel) / (float) player.xpBarCap();
    }

    public static boolean removePlayerXP(EntityPlayer player, int amount) {
        if(getPlayerXP(player) >= amount) {
            addPlayerXP(player, -amount);
            return true;
        }
        return false;
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }*/

    @Nonnull
    public static GlobalPos getGlobalPos(@Nonnull TileFluxDevice tile) {
        return GlobalPos.getPosition(tile.getFluxWorld().getDimensionKey(), tile.getPos());
    }

    public static void writeGlobalPos(@Nonnull CompoundNBT nbt, @Nonnull GlobalPos pos) {
        BlockPos p = pos.getPos();
        nbt.putInt("x", p.getX());
        nbt.putInt("y", p.getY());
        nbt.putInt("z", p.getZ());
        nbt.putString("dim", pos.getDimension().getLocation().toString());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull CompoundNBT nbt) {
        return GlobalPos.getPosition(RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
                new ResourceLocation(nbt.getString("dim"))),
                new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")));
    }

    public static void writeGlobalPos(@Nonnull PacketBuffer buffer, @Nonnull GlobalPos pos) {
        buffer.writeResourceLocation(pos.getDimension().getLocation());
        buffer.writeBlockPos(pos.getPos());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull PacketBuffer buffer) {
        return GlobalPos.getPosition(RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
                buffer.readResourceLocation()), buffer.readBlockPos());
    }

    @Nonnull
    public static String getDisplayPos(@Nonnull GlobalPos pos) {
        BlockPos p = pos.getPos();
        return "X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ();
    }

    @Nonnull
    public static String getDisplayDim(@Nonnull GlobalPos pos) {
        return pos.getDimension().getLocation().toString();
    }

    public static <T> boolean addWithCheck(@Nonnull Collection<T> list, @Nullable T toAdd) {
        if (toAdd != null && !list.contains(toAdd)) {
            list.add(toAdd);
            return true;
        }
        return false;
    }

    /*@Nonnull
    public static ItemStack createItemStackFromBlock(@Nullable World world, BlockPos pos) {
        if (world == null) {
            return new ItemStack(null);
        }
        BlockState state = world.getBlockState(pos);
        return new ItemStack(state.getBlock().asItem());
    }*/

    /*public static boolean addConnection(@Nonnull IFluxDevice fluxDevice) {
        if (fluxDevice.getNetworkID() != -1) {
            IFluxNetwork network = FluxNetworkCache.INSTANCE.getNetwork(fluxDevice.getNetworkID());
            if (network.isValid()) {
                if (fluxDevice.getDeviceType().isController() && network.getConnections(FluxLogicType.CONTROLLER).size() > 0) {
                    return false;
                }
                network.enqueueConnectionAddition(fluxDevice);
                return true;
            }
        }
        return false;
    }

    public static void removeConnection(@Nonnull IFluxDevice fluxDevice, boolean isChunkUnload) {
        if (fluxDevice.getNetworkID() != -1) {
            IFluxNetwork network = FluxNetworkCache.INSTANCE.getNetwork(fluxDevice.getNetworkID());
            if (network.isValid()) {
                network.enqueueConnectionRemoval(fluxDevice, isChunkUnload);
            }
        }
    }*/

    /*public static int getIntFromColor(int red, int green, int blue) {
        red = red << 16 & 0x00FF0000;
        green = green << 8 & 0x0000FF00;
        blue = blue & 0x000000FF;

        return 0xFF000000 | red | green | blue;
    }*/

    public static int getBrighterColor(int color, float factor) {
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;
        red = (int) Math.min(red * factor, 0xff);
        green = (int) Math.min(green * factor, 0xff);
        blue = (int) Math.min(blue * factor, 0xff);
        return red << 16 | green << 8 | blue;
    }

    /**
     * Compact format (like 3.5M)
     *
     * @param in value in
     * @return compact string
     */
    public static String compact(long in) {
        if (in < 1000) {
            return Long.toString(in);
        }
        int level = (int) (Math.log10(in) / 3) - 1;
        char pre = "kMGTPE".charAt(level);
        return String.format("%.1f%c", in * COMPACT_SCALE[level], pre);
    }

    public static String compact(long in, String suffix) {
        if (in < 1000) {
            return in + " " + suffix;
        }
        int level = (int) (Math.log10(in) / 3) - 1;
        char pre = "kMGTPE".charAt(level);
        return String.format("%.1f %c%s", in * COMPACT_SCALE[level], pre, suffix);
    }

    /*public static String format(long in, NumberFormatType style, EnergyType energy, boolean usage) {
        return compact(energy == EnergyType.EU ? in >> 2 : in, style, " " + (usage ? energy.getUsageSuffix() : energy.getStorageSuffix()));
    }*/

    public static boolean isLegalPassword(@Nonnull String str) {
        for (int i = 0; i < str.length(); i++) {
            /*int codePoint;
            char c1 = str.charAt(i);
            if (Character.isHighSurrogate(c1) && i + 1 < str.length()) {
                char c2 = str.charAt(i + 1);
                if (Character.isLowSurrogate(c2)) {
                    codePoint = Character.toCodePoint(c1, c2);
                    ++i;
                } else {
                    codePoint = c1;
                }
            } else {
                codePoint = c1;
            }
            if (codePoint < 0x21 || codePoint >= 0x7f)
                return false;*/
            char c = str.charAt(i);
            if (c < 0x21 || c >= 0x7f)
                return false;
        }
        return true;
    }

    /*@Nullable
    public static <T> T getCap(@Nonnull PlayerEntity player, Capability<T> capability) {
        return cap(player.getCapability(capability));
    }*/

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static <T> T get(@Nonnull LazyOptional<T> lazyOptional) {
        return lazyOptional.orElse(null);
    }

    /*public static CompoundNBT copyConfiguration(TileFluxDevice flux, CompoundNBT config) {
        for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
            type.copy.copyFromTile(config, type.getNBTKey(), flux);
        }
        return config;
    }

    public static void pasteConfiguration(TileFluxDevice flux, CompoundNBT config) {
        for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
            if (config.contains(type.getNBTKey())) {
                type.paste.pasteToTile(config, type.getNBTKey(), flux);
            }
        }
    }*/

    /*public static CompoundNBT getBatchEditingTag(FluxTextWidget a, FluxTextWidget b, FluxTextWidget c, SlidedSwitchButton d, SlidedSwitchButton e, SlidedSwitchButton f) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(ItemFluxDevice.CUSTOM_NAME, a.getText());
        tag.putInt(ItemFluxDevice.PRIORITY, b.getIntegerFromText(false));
        tag.putLong(ItemFluxDevice.LIMIT, c.getLongFromText(true));
        tag.putBoolean(ItemFluxDevice.SURGE_MODE, d != null && d.toggled);
        tag.putBoolean(ItemFluxDevice.DISABLE_LIMIT, e != null && e.toggled);
        tag.putBoolean("chunkLoad", f != null && f.toggled);
        return tag;
    }*/
}
