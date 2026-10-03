package com.tacz.guns.platform.compat;

//? if <1.21.2 {
/*import net.minecraft.util.FastColor;

/^*
 * The color helpers TACZ uses, before 1.21.2 moved them from FastColor.ARGB32 to ARGB.
 ^/
public final class ARGB {
    private ARGB() {
    }

    public static int opaque(int color) {
        return FastColor.ARGB32.opaque(color);
    }

    public static int colorFromFloat(float alpha, float red, float green, float blue) {
        return FastColor.ARGB32.colorFromFloat(alpha, red, green, blue);
    }

    public static int alpha(int color) {
        return FastColor.ARGB32.alpha(color);
    }

    public static int red(int color) {
        return FastColor.ARGB32.red(color);
    }

    public static int green(int color) {
        return FastColor.ARGB32.green(color);
    }

    public static int blue(int color) {
        return FastColor.ARGB32.blue(color);
    }

    public static int lerp(float delta, int from, int to) {
        return FastColor.ARGB32.lerp(delta, from, to);
    }
}
*///?}
