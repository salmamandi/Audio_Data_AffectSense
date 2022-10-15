
package research.sg.edu.edapp.ChartLibs.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import research.sg.edu.edapp.ChartLibs.highlight.Highlight;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;

/**
 * Data object that allows the combination of Line-, Bar-, Scatter-, Bubble- and
 * CandleData. Used in the CombinedChart class.
 *
 * @author Philipp Jahoda
 */
public class CombinedData extends BarLineScatterCandleBubbleData<IBarLineScatterCandleBubbleDataSet<? extends research.sg.edu.edapp.ChartLibs.data.Entry>> {

    private research.sg.edu.edapp.ChartLibs.data.LineData mLineData;
    private research.sg.edu.edapp.ChartLibs.data.BarData mBarData;
    private research.sg.edu.edapp.ChartLibs.data.ScatterData mScatterData;
    private research.sg.edu.edapp.ChartLibs.data.CandleData mCandleData;
    private research.sg.edu.edapp.ChartLibs.data.BubbleData mBubbleData;

    public CombinedData() {
        super();
    }

    public void setData(research.sg.edu.edapp.ChartLibs.data.LineData data) {
        mLineData = data;
        notifyDataChanged();
    }

    public void setData(research.sg.edu.edapp.ChartLibs.data.BarData data) {
        mBarData = data;
        notifyDataChanged();
    }

    public void setData(research.sg.edu.edapp.ChartLibs.data.ScatterData data) {
        mScatterData = data;
        notifyDataChanged();
    }

    public void setData(research.sg.edu.edapp.ChartLibs.data.CandleData data) {
        mCandleData = data;
        notifyDataChanged();
    }

    public void setData(research.sg.edu.edapp.ChartLibs.data.BubbleData data) {
        mBubbleData = data;
        notifyDataChanged();
    }

    @Override
    public void calcMinMax() {

        if(mDataSets == null){
            mDataSets = new ArrayList<>();
        }
        mDataSets.clear();

        mYMax = -Float.MAX_VALUE;
        mYMin = Float.MAX_VALUE;
        mXMax = -Float.MAX_VALUE;
        mXMin = Float.MAX_VALUE;

        mLeftAxisMax = -Float.MAX_VALUE;
        mLeftAxisMin = Float.MAX_VALUE;
        mRightAxisMax = -Float.MAX_VALUE;
        mRightAxisMin = Float.MAX_VALUE;

        List<BarLineScatterCandleBubbleData> allData = getAllData();

        for (research.sg.edu.edapp.ChartLibs.data.ChartData data : allData) {

            data.calcMinMax();

            List<IBarLineScatterCandleBubbleDataSet<? extends research.sg.edu.edapp.ChartLibs.data.Entry>> sets = data.getDataSets();
            mDataSets.addAll(sets);

            if (data.getYMax() > mYMax)
                mYMax = data.getYMax();

            if (data.getYMin() < mYMin)
                mYMin = data.getYMin();

            if (data.getXMax() > mXMax)
                mXMax = data.getXMax();

            if (data.getXMin() < mXMin)
                mXMin = data.getXMin();

            if (data.mLeftAxisMax > mLeftAxisMax)
                mLeftAxisMax = data.mLeftAxisMax;

            if (data.mLeftAxisMin < mLeftAxisMin)
                mLeftAxisMin = data.mLeftAxisMin;

            if (data.mRightAxisMax > mRightAxisMax)
                mRightAxisMax = data.mRightAxisMax;

            if (data.mRightAxisMin < mRightAxisMin)
                mRightAxisMin = data.mRightAxisMin;

        }
    }

    public research.sg.edu.edapp.ChartLibs.data.BubbleData getBubbleData() {
        return mBubbleData;
    }

    public LineData getLineData() {
        return mLineData;
    }

    public BarData getBarData() {
        return mBarData;
    }

    public ScatterData getScatterData() {
        return mScatterData;
    }

    public CandleData getCandleData() {
        return mCandleData;
    }

    /**
     * Returns all data objects in row: line-bar-scatter-candle-bubble if not null.
     *
     * @return
     */
    public List<BarLineScatterCandleBubbleData> getAllData() {

        List<BarLineScatterCandleBubbleData> data = new ArrayList<BarLineScatterCandleBubbleData>();
        if (mLineData != null)
            data.add(mLineData);
        if (mBarData != null)
            data.add(mBarData);
        if (mScatterData != null)
            data.add(mScatterData);
        if (mCandleData != null)
            data.add(mCandleData);
        if (mBubbleData != null)
            data.add(mBubbleData);

        return data;
    }

    public BarLineScatterCandleBubbleData getDataByIndex(int index) {
        return getAllData().get(index);
    }

    @Override
    public void notifyDataChanged() {
        if (mLineData != null)
            mLineData.notifyDataChanged();
        if (mBarData != null)
            mBarData.notifyDataChanged();
        if (mCandleData != null)
            mCandleData.notifyDataChanged();
        if (mScatterData != null)
            mScatterData.notifyDataChanged();
        if (mBubbleData != null)
            mBubbleData.notifyDataChanged();

        calcMinMax(); // recalculate everything
    }

    /**
     * Get the Entry for a corresponding highlight object
     *
     * @param highlight
     * @return the entry that is highlighted
     */
    @Override
    public research.sg.edu.edapp.ChartLibs.data.Entry getEntryForHighlight(Highlight highlight) {

        List<BarLineScatterCandleBubbleData> dataObjects = getAllData();

        if (highlight.getDataIndex() >= dataObjects.size())
            return null;

        research.sg.edu.edapp.ChartLibs.data.ChartData data = dataObjects.get(highlight.getDataIndex());

        if (highlight.getDataSetIndex() >= data.getDataSetCount())
            return null;
        else {
            // The value of the highlighted entry could be NaN -
            //   if we are not interested in highlighting a specific value.

            List<research.sg.edu.edapp.ChartLibs.data.Entry> entries = data.getDataSetByIndex(highlight.getDataSetIndex())
                    .getEntriesForXValue(highlight.getX());
            for (research.sg.edu.edapp.ChartLibs.data.Entry entry : entries)
                if (entry.getY() == highlight.getY() ||
                        Float.isNaN(highlight.getY()))
                    return entry;

            return null;
        }
    }

    public int getDataIndex(research.sg.edu.edapp.ChartLibs.data.ChartData data) {
        return getAllData().indexOf(data);
    }

    @Override
    public boolean removeDataSet(IBarLineScatterCandleBubbleDataSet<? extends research.sg.edu.edapp.ChartLibs.data.Entry> d) {

        List<BarLineScatterCandleBubbleData> datas = getAllData();

        boolean success = false;

        for (ChartData data : datas) {

            success = data.removeDataSet(d);

            if (success) {
                break;
            }
        }

        return success;
    }

    @Deprecated
    @Override
    public boolean removeDataSet(int index) {
        Log.e("MPAndroidChart", "removeDataSet(int index) not supported for CombinedData");
        return false;
    }

    @Deprecated
    @Override
    public boolean removeEntry(Entry e, int dataSetIndex) {
        Log.e("MPAndroidChart", "removeEntry(...) not supported for CombinedData");
        return false;
    }

    @Deprecated
    @Override
    public boolean removeEntry(float xValue, int dataSetIndex) {
        Log.e("MPAndroidChart", "removeEntry(...) not supported for CombinedData");
        return false;
    }
}
