package common;

import java.awt.image.BufferedImage;

public class ModdedBufferedImage extends BufferedImage {

    private boolean fakeType = false;

    public ModdedBufferedImage(int width, int height, int imageType) {
        super(width, height, imageType);
    }

    public void setFakeType(boolean ft) {
        fakeType = ft;
    }

    @Override
    public int getType() {
        if (fakeType) {
            return BufferedImage.TYPE_INT_ARGB_PRE;
        }
        return super.getType();
    }

}
