package org.craftarix.monitoring.util;

import com.mojang.authlib.GameProfile;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class ReflectionUtil {
    private static Class resolvableProfileClass = null;

    static {
        try {
            resolvableProfileClass = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public Object getResolvableProfile(GameProfile gameProfile) {
        Object profile = null;
        if (resolvableProfileClass == null) {
            profile = gameProfile;
        } else {
            try {
                profile = resolvableProfileClass.getConstructor(GameProfile.class).newInstance(gameProfile);
            } catch (Exception ignored) {

            }
        }

        return profile;
    }

    public static void setField(Object object, String field, Object fieldValue) {
        Field f = null;
        Class<?> clazz = object.getClass();
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(object, fieldValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
            f.setAccessible(false);
        }
    }
}
