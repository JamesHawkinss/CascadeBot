/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */


package org.cascadebot.cascadebot.utils;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.messaging.MessageType;
import org.cascadebot.cascadebot.messaging.MessagingObjects;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorUtils {

    private static final Pattern HEX_COLOR = Pattern.compile("#([A-Fa-f0-9]+)");
    private static final Pattern DECIMAL_COLOR = Pattern.compile("([0-9]{1,8})");
    private static final Pattern RGB_COLOR = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");
    private static final Pattern BINARY_COLOR = Pattern.compile("([0-1]+)");

    public String getHex(int r, int g, int b) {
        return String.format("%02x%02x%02x", r, g, b);
    }

    public MessageEmbed getColor(String text, CommandContext context) {

        Color color = null;
        Matcher matcher;

        try {
            color = (Color) Color.class.getField(text.toUpperCase()).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            //RGB_COLOR Values
            if ((matcher = RGB_COLOR.matcher(text)).find()) {
                try {
                    color = new Color(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3)));
                } catch (IllegalArgumentException e1) {
                    context.getTypedMessaging().replyDanger(context.i18n("utils.color_command.valid_rgb"));
                    return null;
                }
                //Hex
            } else if ((matcher = HEX_COLOR.matcher(text)).matches()) {
                try {
                    color = Color.decode(matcher.group());
                } catch (NumberFormatException e1) {
                    context.getTypedMessaging().replyDanger(context.i18n("utils.color_command.valid_hex_code"));
                    return null;
                }
                //Decimal
            } else if ((matcher = DECIMAL_COLOR.matcher(text)).matches()) {
                try {
                    color = Color.decode(matcher.group());
                } catch (NumberFormatException e1) {
                    context.getTypedMessaging().replyDanger(context.i18n("utils.color_command.valid_decimal"));
                    return null;
                }
                //Binary
            } else if ((matcher = BINARY_COLOR.matcher(text)).matches()) {
                try {
                    color = Color.decode(String.valueOf(Integer.parseUnsignedInt(matcher.group(), 2)));
                } catch (NumberFormatException e1) {
                    context.getTypedMessaging().replyDanger(context.i18n("utils.color_command.valid_binary"));
                }
            }
        }

        if (color == null) {
            context.getTypedMessaging().replyDanger(context.i18n("utils.color_command.color_not_recognised"));
            return null;
        }

        String rgbValues = color.getRed() + "," + color.getGreen() + "," + color.getBlue();
        String hex = getHex(color.getRed(), color.getGreen(), color.getBlue());
        int decimalColor = Integer.parseUnsignedInt(hex, 16);

        EmbedBuilder builder = MessagingObjects.getMessageTypeEmbedBuilder(MessageType.INFO, context.getUser());
        builder.setTitle(context.i18n("utils.color_command.embed_title", hex));
        builder.setColor(color);
        builder.addField(context.i18n("utils.color_command.color.rgb"), rgbValues, true); // RGB Values
        builder.addField(context.i18n("utils.color_command.decimal"), Integer.toUnsignedString(decimalColor), true); // Decimal Value
        builder.addField(context.i18n("utils.color_command.binary"), Integer.toBinaryString(decimalColor), true); // Binary Value
        return builder.build();
    };

}
