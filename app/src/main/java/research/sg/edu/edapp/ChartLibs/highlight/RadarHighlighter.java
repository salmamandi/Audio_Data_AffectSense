package research.sg.edu.edapp.ChartLibs.highlight;

import java.util.List;

import research.sg.edu.edapp.ChartLibs.charts.RadarChart;
import research.sg.edu.edapp.ChartLibs.data.Entry;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IDataSet;
import research.sg.edu.edapp.ChartLibs.utils.MPPointF;
import research.sg.edu.edapp.ChartLibs.utils.Utils;

/**
 * Created by philipp on 12/06/16.
 */
public class RadarHighlighter extends PieRadarHighlighter<RadarChart> {

    public RadarHighlighter(RadarChart chart) {
        super(chart);
    }

    @Override
    protected research.sg.edu.edapp.ChartLibs.highlight.Highlight getClosestHighlight(int index, float x, float y) {

        List<research.sg.edu.edapp.ChartLibs.highlight.Highlight> highlights = getHighlightsAtIndex(index);

        float distanceToCenter = mChart.distanceToCenter(x, y) / mChart.getFactor();

        research.sg.edu.edapp.ChartLibs.highlight.Highlight closest = null;
        float distance = Float.MAX_VALUE;

        for (int i = 0; i < highlights.size(); i++) {

            research.sg.edu.edapp.ChartLibs.highlight.Highlight high = highlights.get(i);

            float cdistance = Math.abs(high.getY() - distanceToCenter);
            if (cdistance < distance) {
                closest = high;
                distance = cdistance;
            }
        }

        return closest;
    }
    /**
     * Returns an array of Highlight objects for the given index. The Highlight
     * objects give information about the value at the selected index and the
     * DataSet it belongs to. INFORMATION: This method does calculations at
     * runtime. Do not over-use in performance critical situations.
     *
     * @param index
     * @return
     */
    protected List<research.sg.edu.edapp.ChartLibs.highlight.Highlight> getHighlightsAtIndex(int index) {

        mHighlightBuffer.clear();

        float phaseX = mChart.getAnimator().getPhaseX();
        float phaseY = mChart.getAnimator().getPhaseY();
        float sliceangle = mChart.getSliceAngle();
        float factor = mChart.getFactor();

        MPPointF pOut = MPPointF.getInstance(0,0);
        for (int i = 0; i < mChart.getData().getDataSetCount(); i++) {

            IDataSet<?> dataSet = mChart.getData().getDataSetByIndex(i);

            final Entry entry = dataSet.getEntryForIndex(index);

            float y = (entry.getY() - mChart.getYChartMin());

            Utils.getPosition(
                    mChart.getCenterOffsets(), y * factor * phaseY,
                    sliceangle * index * phaseX + mChart.getRotationAngle(), pOut);

            mHighlightBuffer.add(new research.sg.edu.edapp.ChartLibs.highlight.Highlight(index, entry.getY(), pOut.x, pOut.y, i, dataSet.getAxisDependency()));
        }

        return mHighlightBuffer;
    }
}
