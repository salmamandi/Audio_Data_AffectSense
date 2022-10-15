
package research.sg.edu.edapp.ChartLibs.data;

import java.util.ArrayList;
import java.util.List;

import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IBubbleDataSet;
import research.sg.edu.edapp.ChartLibs.utils.Utils;

public class BubbleDataSet extends BarLineScatterCandleBubbleDataSet<research.sg.edu.edapp.ChartLibs.data.BubbleEntry> implements IBubbleDataSet {

    protected float mMaxSize;
    protected boolean mNormalizeSize = true;

    private float mHighlightCircleWidth = 2.5f;

    public BubbleDataSet(List<research.sg.edu.edapp.ChartLibs.data.BubbleEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public void setHighlightCircleWidth(float width) {
        mHighlightCircleWidth = Utils.convertDpToPixel(width);
    }

    @Override
    public float getHighlightCircleWidth() {
        return mHighlightCircleWidth;
    }

    @Override
    protected void calcMinMax(research.sg.edu.edapp.ChartLibs.data.BubbleEntry e) {
        super.calcMinMax(e);

        final float size = e.getSize();

        if (size > mMaxSize) {
            mMaxSize = size;
        }
    }

    @Override
    public research.sg.edu.edapp.ChartLibs.data.DataSet<research.sg.edu.edapp.ChartLibs.data.BubbleEntry> copy() {

        List<research.sg.edu.edapp.ChartLibs.data.BubbleEntry> yVals = new ArrayList<BubbleEntry>();

        for (int i = 0; i < mValues.size(); i++) {
            yVals.add(mValues.get(i).copy());
        }

        BubbleDataSet copied = new BubbleDataSet(yVals, getLabel());
        copied.mColors = mColors;
        copied.mHighLightColor = mHighLightColor;

        return copied;
    }

    @Override
    public float getMaxSize() {
        return mMaxSize;
    }

    @Override
    public boolean isNormalizeSizeEnabled() {
        return mNormalizeSize;
    }

    public void setNormalizeSizeEnabled(boolean normalizeSize) {
        mNormalizeSize = normalizeSize;
    }
}
