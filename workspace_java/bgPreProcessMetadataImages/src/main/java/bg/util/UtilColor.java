package bg.util;

import java.awt.Color;

public final class UtilColor {

  public static Color colorFor(int index1to1000) {
    if (index1to1000 < 1 || index1to1000 > 1000) {
      throw new IllegalArgumentException("index must be in [1..1000]");
    }

    int x = index1to1000;

    // petit hash 32-bit (mix) déterministe
    x ^= (x >>> 16);
    x *= 0x7feb352d;
    x ^= (x >>> 15);
    x *= 0x846ca68b;
    x ^= (x >>> 16);

    // Hue sur [0..1)
    float hue = (x & 0xFFFF) / 65536f;

    // S/V en 2 ou 3 niveaux pour aider le contraste
    float saturation = ((x >>> 16) & 1) == 0 ? 0.85f : 0.55f;
    float brightness = ((x >>> 17) & 1) == 0 ? 0.90f : 0.65f;

    return Color.getHSBColor(hue, saturation, brightness);
  }
}