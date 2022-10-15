package research.sg.edu.edapp.ChartLibs.renderer;

import research.sg.edu.edapp.ChartLibs.animation.ChartAnimator;
import research.sg.edu.edapp.ChartLibs.data.DataSet;
import research.sg.edu.edapp.ChartLibs.data.Entry;
import research.sg.edu.edapp.ChartLibs.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IDataSet;
import research.sg.edu.edapp.ChartLibs.utils.ViewPortHandler;

/**
 * Created by Philipp Jahoda on 09/06/16.
 */
public abstract class BarLineScatterCandleBubbleRenderer extends research.sg.edu.edapp.ChartLibs.renderer.DataRenderer {

    /**
     * buffer for storing the current minimum and maximum visible x
     */
    protected XBounds mXBounds = new XBounds();

    public BarLineScatterCandleBubbleRenderer(ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
    }

    /**
     * Returns true if the DataSet values should be drawn, false if not.
     *
     * @param set
     * @return
     */
    protected boolean shouldDrawValues(IDataSet set) {
        return set.isVisible() && set.isDrawValuesEnabled();
    }

    /**
     * Checks if the provided entry object is in bounds for drawing considering the current animation phase.
     *
     * @param e
     * @param set
     * @return
     */
    protected boolean isInBoundsX(Entry e, IBarLineScatterCandleBubbleDataSet set) {

        if (e == null)
            return false;

        float entryIndex = set.getEntryIndex(e);

        return !(e == null || entryIndex >= set.getEntryCount() * mAnimator.getPhaseX());
    }

    /**
     * Class representing the bounds of the current viewport in terms of indices in the values array of a DataSet.
     */
    protected class XBounds {

        /**
         * minimum visible entry index
         */
        public int min;

        /**
         * maximum visible entry index
         */
        public int max;

        /**
         * range of visible entry indices
         */
        public int range;

        /**
         * Calculates the minimum and maximum x values as well as the range between them.
         *
         * @param chart
         * @param dataSet
         */
        public void set(BarLineScatterCandleBubbleDataProvider chart, IBarLineScatterCandleBubbleDataSet dataSet) {
            float phaseX = Math.max(0.f, Math.min(1.f, mAnimator.getPhaseX()));

            float low = chart.getLowestVisibleX();
            float high = chart.getHighestVisibleX();

            Entry entryFrom = dataSet.getEntryForXValue(low, Float.NaN, DataSet.Rounding.DOWN);
            Entry entryTo = dataSet.getEntryForXValue(high, Float.NaN, DataSet.Rounding.UP);

            min = entryFrom == null ? 0 : dataSet.getEntryIndex(entryFrom);
            max = entryTo == null ? 0 : dataSet.getEntryIndex(entryTo);
            range = (int) ((max - min) * phaseX);
        }
    }
}
