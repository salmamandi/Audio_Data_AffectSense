package research.sg.edu.edapp.ChartLibs.renderer;

import android.graphics.Canvas;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import research.sg.edu.edapp.ChartLibs.animation.ChartAnimator;
import research.sg.edu.edapp.ChartLibs.charts.Chart;
import research.sg.edu.edapp.ChartLibs.charts.CombinedChart;
import research.sg.edu.edapp.ChartLibs.charts.CombinedChart.DrawOrder;
import research.sg.edu.edapp.ChartLibs.data.ChartData;
import research.sg.edu.edapp.ChartLibs.data.CombinedData;
import research.sg.edu.edapp.ChartLibs.highlight.Highlight;
import research.sg.edu.edapp.ChartLibs.utils.ViewPortHandler;

/**
 * Renderer class that is responsible for rendering multiple different data-types.
 */
public class CombinedChartRenderer extends research.sg.edu.edapp.ChartLibs.renderer.DataRenderer {

    /**
     * all rederers for the different kinds of data this combined-renderer can draw
     */
    protected List<DataRenderer> mRenderers = new ArrayList<DataRenderer>(5);

    protected WeakReference<Chart> mChart;

    public CombinedChartRenderer(CombinedChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = new WeakReference<Chart>(chart);
        createRenderers();
    }

    /**
     * Creates the renderers needed for this combined-renderer in the required order. Also takes the DrawOrder into
     * consideration.
     */
    public void createRenderers() {

        mRenderers.clear();

        CombinedChart chart = (CombinedChart)mChart.get();
        if (chart == null)
            return;

        DrawOrder[] orders = chart.getDrawOrder();

        for (DrawOrder order : orders) {

            switch (order) {
                case BAR:
                    if (chart.getBarData() != null)
                        mRenderers.add(new research.sg.edu.edapp.ChartLibs.renderer.BarChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case BUBBLE:
                    if (chart.getBubbleData() != null)
                        mRenderers.add(new research.sg.edu.edapp.ChartLibs.renderer.BubbleChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case LINE:
                    if (chart.getLineData() != null)
                        mRenderers.add(new research.sg.edu.edapp.ChartLibs.renderer.LineChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case CANDLE:
                    if (chart.getCandleData() != null)
                        mRenderers.add(new CandleStickChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case SCATTER:
                    if (chart.getScatterData() != null)
                        mRenderers.add(new research.sg.edu.edapp.ChartLibs.renderer.ScatterChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
            }
        }
    }

    @Override
    public void initBuffers() {

        for (research.sg.edu.edapp.ChartLibs.renderer.DataRenderer renderer : mRenderers)
            renderer.initBuffers();
    }

    @Override
    public void drawData(Canvas c) {

        for (research.sg.edu.edapp.ChartLibs.renderer.DataRenderer renderer : mRenderers)
            renderer.drawData(c);
    }

    @Override
    public void drawValues(Canvas c) {

        for (research.sg.edu.edapp.ChartLibs.renderer.DataRenderer renderer : mRenderers)
            renderer.drawValues(c);
    }

    @Override
    public void drawExtras(Canvas c) {

        for (research.sg.edu.edapp.ChartLibs.renderer.DataRenderer renderer : mRenderers)
            renderer.drawExtras(c);
    }

    protected List<Highlight> mHighlightBuffer = new ArrayList<Highlight>();

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        Chart chart = mChart.get();
        if (chart == null) return;

        for (research.sg.edu.edapp.ChartLibs.renderer.DataRenderer renderer : mRenderers) {
            ChartData data = null;

            if (renderer instanceof research.sg.edu.edapp.ChartLibs.renderer.BarChartRenderer)
                data = ((research.sg.edu.edapp.ChartLibs.renderer.BarChartRenderer)renderer).mChart.getBarData();
            else if (renderer instanceof research.sg.edu.edapp.ChartLibs.renderer.LineChartRenderer)
                data = ((LineChartRenderer)renderer).mChart.getLineData();
            else if (renderer instanceof CandleStickChartRenderer)
                data = ((CandleStickChartRenderer)renderer).mChart.getCandleData();
            else if (renderer instanceof research.sg.edu.edapp.ChartLibs.renderer.ScatterChartRenderer)
                data = ((ScatterChartRenderer)renderer).mChart.getScatterData();
            else if (renderer instanceof research.sg.edu.edapp.ChartLibs.renderer.BubbleChartRenderer)
                data = ((BubbleChartRenderer)renderer).mChart.getBubbleData();

            int dataIndex = data == null ? -1
                    : ((CombinedData)chart.getData()).getAllData().indexOf(data);

            mHighlightBuffer.clear();

            for (Highlight h : indices) {
                if (h.getDataIndex() == dataIndex || h.getDataIndex() == -1)
                    mHighlightBuffer.add(h);
            }

            renderer.drawHighlighted(c, mHighlightBuffer.toArray(new Highlight[mHighlightBuffer.size()]));
        }
    }

    /**
     * Returns the sub-renderer object at the specified index.
     *
     * @param index
     * @return
     */
    public research.sg.edu.edapp.ChartLibs.renderer.DataRenderer getSubRenderer(int index) {
        if (index >= mRenderers.size() || index < 0)
            return null;
        else
            return mRenderers.get(index);
    }

    /**
     * Returns all sub-renderers.
     *
     * @return
     */
    public List<DataRenderer> getSubRenderers() {
        return mRenderers;
    }

    public void setSubRenderers(List<DataRenderer> renderers) {
        this.mRenderers = renderers;
    }
}
