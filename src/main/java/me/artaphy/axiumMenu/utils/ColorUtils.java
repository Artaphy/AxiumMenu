package me.artaphy.axiumMenu.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Utility class for handling color codes and formatting in text.
 * Supports:
 * - Traditional color codes (&)
 * - Hex colors (<#RRGGBB>)
 * - Gradients (<gradient:#color1:#color2>text</gradient>)
 * - Rainbow text (<rainbow>text</rainbow>)
 * - Format codes (bold, italic, etc.)
 * <p>
 * This class implements caching to improve performance for frequently used strings.
 */
public class ColorUtils {
   private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
   private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(?:gradient|g)(?:#\\d+)?(?::#([A-Fa-f\\d]{6}|[A-Fa-f\\d]{3})){2,}(?::(?:l|L|loop))?>");
   private static final Pattern RAINBOW_PATTERN = Pattern.compile("<(?:rainbow|r)(?:#\\d+)?(?::\\d*\\.?\\d+)?(?::\\d*\\.?\\d+)?(?::(?:l|L|loop))?>");
   private static final Pattern FORMAT_PATTERN = Pattern.compile("<([a-zA-Z]+)>");
   private static final Map<String, String> colorCache = new ConcurrentHashMap<>();

   /**
    * Applies color codes and formatting to a message.
    *
    * @param message The message to colorize
    * @return The colorized message
    */
   public static String colorize(String message) {
       return colorCache.computeIfAbsent(message, ColorUtils::colorizeUncached);
   }

   private static String colorizeUncached(String message) {
       if (message == null) return null;
       
       message = ChatColor.translateAlternateColorCodes('&', message);
       message = translateHexColorCodes(message);
       message = translateGradientCodes(message);
       message = translateRainbowCodes(message);
       message = translateFormatCodes(message);
       
       return message;
   }

   private static String translateHexColorCodes(String message) {
       Matcher matcher = HEX_PATTERN.matcher(message);
       StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
       while (matcher.find()) {
           String group = matcher.group(1);
           matcher.appendReplacement(buffer, ChatColor.of("#" + group).toString());
       }
       return matcher.appendTail(buffer).toString();
   }

   private static String translateGradientCodes(String message) {
       Matcher matcher = GRADIENT_PATTERN.matcher(message);
       StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
       while (matcher.find()) {
           String gradientString = matcher.group();
           List<Color> colors = extractColors(gradientString);
           boolean loop = gradientString.toLowerCase().endsWith(":l") || gradientString.toLowerCase().endsWith(":loop");
           
           String content = message.substring(matcher.end(), findEndTag(message, matcher.end()));
           String gradientContent = applyGradient(content, colors, loop);
           
           matcher.appendReplacement(buffer, gradientContent);
           message = message.substring(findEndTag(message, matcher.end()));
           matcher = GRADIENT_PATTERN.matcher(message);
       }
       return matcher.appendTail(buffer).toString();
   }

   private static String translateRainbowCodes(String message) {
       Matcher matcher = RAINBOW_PATTERN.matcher(message);
       StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
       while (matcher.find()) {
           String rainbowString = matcher.group();
           double frequency = 0.1;
           double saturation = 1.0;
           boolean loop = rainbowString.toLowerCase().endsWith(":l") || rainbowString.toLowerCase().endsWith(":loop");
           
           String[] parts = rainbowString.split(":");
           if (parts.length > 1) {
               try {
                   frequency = Double.parseDouble(parts[1]);
               } catch (NumberFormatException ignored) {}
           }
           if (parts.length > 2) {
               try {
                   saturation = Double.parseDouble(parts[2]);
               } catch (NumberFormatException ignored) {}
           }
           
           String content = message.substring(matcher.end(), findEndTag(message, matcher.end()));
           String rainbowContent = applyRainbow(content, frequency, saturation, loop);
           
           matcher.appendReplacement(buffer, rainbowContent);
           message = message.substring(findEndTag(message, matcher.end()));
           matcher = RAINBOW_PATTERN.matcher(message);
       }
       return matcher.appendTail(buffer).toString();
   }

   private static String translateFormatCodes(String message) {
       Matcher matcher = FORMAT_PATTERN.matcher(message);
       StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
       while (matcher.find()) {
           String format = matcher.group(1).toLowerCase();
           ChatColor color = switch (format) {
               case "bold" -> ChatColor.BOLD;
               case "italic" -> ChatColor.ITALIC;
               case "underline" -> ChatColor.UNDERLINE;
               case "strikethrough" -> ChatColor.STRIKETHROUGH;
               case "magic" -> ChatColor.MAGIC;
               case "reset" -> ChatColor.RESET;
               default -> ChatColor.WHITE;
           };
           matcher.appendReplacement(buffer, color.toString());
       }
       return matcher.appendTail(buffer).toString();
   }

   private static List<Color> extractColors(String gradientString) {
       List<Color> colors = new ArrayList<>();
       Matcher colorMatcher = Pattern.compile("#([A-Fa-f0-9]{6})").matcher(gradientString);
       while (colorMatcher.find()) {
           colors.add(Color.decode("#" + colorMatcher.group(1)));
       }
       return colors;
   }

   private static String applyGradient(String content, List<Color> colors, boolean loop) {
       StringBuilder result = new StringBuilder();
       int stepCount = content.length() - 1;
       for (int i = 0; i < content.length(); i++) {
           if (loop) {
               int colorIndex = (int) ((float) i / content.length() * colors.size()) % colors.size();
               int nextColorIndex = (colorIndex + 1) % colors.size();
               Color color = interpolateColor(colors.get(colorIndex), colors.get(nextColorIndex), (float) (i % (content.length() / colors.size())) / ((float) content.length() / colors.size()));
               result.append(ChatColor.of(color)).append(content.charAt(i));
           } else {
               float ratio = (float) i / stepCount;
               int colorIndex = (int) (ratio * (colors.size() - 1));
               Color color = interpolateColor(colors.get(colorIndex), colors.get(Math.min(colorIndex + 1, colors.size() - 1)), ratio * (colors.size() - 1) - colorIndex);
               result.append(ChatColor.of(color)).append(content.charAt(i));
           }
       }
       return result.toString();
   }

   private static String applyRainbow(String content, double frequency, double saturation, boolean loop) {
       StringBuilder result = new StringBuilder();
       for (int i = 0; i < content.length(); i++) {
           double hue = (frequency * i + (loop ? 0 : i / (double) content.length())) % 1.0;
           Color color = Color.getHSBColor((float) hue, (float) saturation, 1.0f);
           result.append(ChatColor.of(color)).append(content.charAt(i));
       }
       return result.toString();
   }

   private static Color interpolateColor(Color color1, Color color2, float factor) {
       int red = (int) (color1.getRed() * (1 - factor) + color2.getRed() * factor);
       int green = (int) (color1.getGreen() * (1 - factor) + color2.getGreen() * factor);
       int blue = (int) (color1.getBlue() * (1 - factor) + color2.getBlue() * factor);
       return new Color(red, green, blue);
   }

   private static int findEndTag(String message, int startIndex) {
       int tagCount = 1;
       for (int i = startIndex; i < message.length(); i++) {
           if (message.charAt(i) == '<') tagCount++;
           if (message.charAt(i) == '>') tagCount--;
           if (tagCount == 0) return i + 1;
       }
       return message.length();
   }
}
