package com.strangeone101.platinumarenas;

import com.strangeone101.platinumarenas.commands.BorderCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.swing.text.html.Option;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Util {

    public static String permissionsMessage() {
        return translatableOrError("feedback.permissions");
    }

    public static String translatablePrefix(String key) {
        return PlatinumArenas.PREFIX + ChatColor.RESET + " " + translatableOrError(key);
    }

    public static String translatableOrError(String key) {
        return translatable(key).orElse("&cError: The lang key '%s' could not be found!");
    }

    public static Optional<String> translatable(String key) {
        String value = PlatinumArenas.languageFile.getString(key);
        return Optional.ofNullable(value);
    }

    private static final Pattern hexColorCodes = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String color(String message) {
        Matcher matcher = hexColorCodes.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = hexColorCodes.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        if (pos + (pattern.length - 1) > input.length) return false;
        for(int i=0; i< pattern.length; i++) {
            if(pattern[i] != input[pos+i]) {
                return false;
            }
        }
        return true;
    }

    public static List<byte[]> split(byte[] pattern, byte[] input) {
        List<byte[]> l = new LinkedList<byte[]>();
        int blockStart = 0;
        for(int i=0; i<input.length; i++) {
            if(isMatch(pattern,input,i)) {
                l.add(Arrays.copyOfRange(input, blockStart, i));
                blockStart = i+pattern.length;
                i = blockStart;
            }
        }
        l.add(Arrays.copyOfRange(input, blockStart, input.length ));
        return l;
    }

    public static boolean isLocationWithin(Location min, Location max, Location test) {
        int x1 = min.getBlockX();
        int x2 = max.getBlockX();
        int y1 = min.getBlockY();
        int y2 = max.getBlockY();
        int z1 = min.getBlockZ();
        int z2 = max.getBlockZ();

        if (x1 > x2) { //Flip variables to make sure x1 is smaller
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }

        if (y1 > y2) { //Flip variables to make sure y1 is smaller
            int temp = y2;
            y2 = y1;
            y1 = temp;
        }

        if (z1 > z2) { //Flip variables to make sure z1 is smaller
            int temp = z2;
            z2 = z1;
            z1 = temp;
        }

        if (test.getX() >= x1 && test.getX() <= x2 && test.getY() >= y1 && test.getY() <= y2 && test.getZ() >= z1 && test.getZ() <= z2) {
            return true;
        }

        return false;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static byte[] compress(byte[] bytes) {
        Deflater compresser = new Deflater();
        compresser.setInput(bytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
        compresser.finish();
        byte[] buffer = new byte[1024]; //Compress in 1kb lots
        while (!compresser.finished()) {
            int count = compresser.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        compresser.end();
        return outputStream.toByteArray();
    }

    public static byte[] decompress(byte[] bytes) {
        Inflater decompresser = new Inflater();
        decompresser.setInput(bytes);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
            decompresser.setInput(bytes);
            byte[] buffer = new byte[1024]; //Decompress in 1kb lots
            while (!decompresser.finished()) {
                int count = decompresser.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Copies a resource located in the jar to a file.
     *
     * @param resourceName The filename of the resource to copy
     * @param output The file location to copy it to. Should not exist.
     * @return True if the operation succeeded.
     */
    public static boolean saveResource(String resourceName, File output) {
        if (PlatinumArenas.INSTANCE.getResource(resourceName) == null) return false;

        try {
            InputStream in = PlatinumArenas.INSTANCE.getResource(resourceName);

            OutputStream out = new FileOutputStream(output);
            byte[] buf = new byte[256];
            int len;

            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }

            out.close();
            in.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isPre116Wall(String data) {
        String type = data.split("\\[")[0].toLowerCase(Locale.ROOT);
        if (type.contains(":")) type = data.split(":")[1];

        switch (type) {
            case "cobblestone_wall":
            case "mossy_cobblestone_wall":
            case "andesite_wall":
            case "granite_wall":
            case "diorite_wall":
            case "stone_brick_wall":
            case "mossy_stone_brick_wall":
            case "sandstone_wall":
            case "brick_wall":
            case "red_sandstone_wall":
            case "prismarine_wall":
            case "nether_brick_wall":
            case "red_nether_brick_wall":
            case "end_stone_brick_wall":
                return true;
            default:
                return false;
        }
    }

    public static Map<String, String> convertBlockstateData(String data) {
        if (data.contains("[")) {
            data = data.split("\\[")[1].split("]")[0];
        }

        Map<String, String> mapData = new HashMap<>();

        for (String s : data.split(",")) {
            String key = s.split("=")[0];
            String value = s.split("=")[1];
            mapData.put(key, value);
        }

        return mapData;
    }

    public static String convertBlockstateData(Map<String, String> mapData) {
        return mapData.keySet().stream().map(key -> key + "=" + mapData.get(key)).collect(Collectors.joining(","));
    }

    /**
     * Returns the version of your server
     *
     * @return The server version
     */
    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    public static String getCraftbukkitClass(String key) {
        return "org.bukkit.craftbukkit." + getServerVersion() + "." + key;
    }
}
