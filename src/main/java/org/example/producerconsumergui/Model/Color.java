package org.example.producerconsumergui.Model;

/*
Add any colors you need
 */
public enum Color {
    RED("#FF5252"),    // Bright Coral Red
    ORANGE("#FFAB40"), // Amber Orange
    YELLOW("#FFFF8D"), // Soft Canary Yellow
    GREEN("#69F0AE"),  // Mint Green
    BLUE("#448AFF"),   // Royal Blue
    PURPLE("#E040FB"), // Vivid Orchid
    PINK("#FF4081");   // Hot Pink
    private final String hexCode ;
    Color(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }
}
