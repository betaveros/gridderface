// Based on code from Romain Guy's BlendComposite, available here (under the
// BSD license):
// http://www.curious-creature.org/2006/09/20/new-blendings-modes-for-java2d/
// I'm assuming my "based on" is weak enough that this notice suffices.
package gridderface;

import java.awt.*;
import java.awt.image.*;

public class MultiplyComposite implements Composite {
	private float alpha;
	public MultiplyComposite(float alpha) {
		this.alpha = alpha;
	}
	public CompositeContext createContext(
			ColorModel srcColorModel,
			ColorModel dstColorModel,
			RenderingHints hints) {
		return new MultiplyContext(alpha);
	}
	private static class MultiplyContext extends ComponentContext {
		private MultiplyContext(float alpha) { super(alpha); }
		public int combine(int s, int d, float srcAlpha) {
			return d + (int) (((s*d / 255) - d) * alpha * srcAlpha);
		}
	}
}

