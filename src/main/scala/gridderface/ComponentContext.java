package gridderface;

import java.awt.*;
import java.awt.image.*;

public abstract class ComponentContext implements CompositeContext {
	protected float alpha;
	public ComponentContext(float alpha) {
		this.alpha = alpha;
	}
	public abstract int combine(int s, int d, float srcAlpha);
	public int combineAlpha(int s, int d) {
		if (d == 255) return 255; // as used, this should always be true
		// (partly premature optimization but partly I don't trust myself to
		// get the below formula completely right anyway)
		return Math.min(d + (int) ((255-d) * s * alpha / 255f), 255);
	}
	public void dispose() {}
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
				dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
				dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
			throw new AssertionError("MultiplyComposite called to compose non-INT rasters");
		}

		int width = Math.min(src.getWidth(), dstIn.getWidth());
		int height = Math.min(src.getHeight(), dstIn.getHeight());

		int[] srcPixels = new int[width];
		int[] dstPixels = new int[width];

		for (int y = 0; y < height; y++) {
			src.getDataElements(0, y, width, 1, srcPixels);
			dstIn.getDataElements(0, y, width, 1, dstPixels);
			for (int x = 0; x < width; x++) {
				int dstPix = 0;
				float srcAlpha = (srcPixels[x] >>> 24) / 255;
				for (int sh = 0; sh < 24; sh += 8) {
					dstPix |= combine(
							(srcPixels[x] >> sh) & 0xff,
							(dstPixels[x] >> sh) & 0xff, srcAlpha) << sh;
				}
				dstPix |= combineAlpha(srcPixels[x] >>> 24, dstPixels[x] >>> 24) << 24;
				dstPixels[x] = dstPix;
			}
			dstOut.setDataElements(0, y, width, 1, dstPixels);
		}
	}
}
