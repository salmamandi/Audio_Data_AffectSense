
package research.sg.edu.edapp.ChartLibs.charts;

import android.content.Context;
import android.util.AttributeSet;

import research.sg.edu.edapp.ChartLibs.data.CandleData;
import research.sg.edu.edapp.ChartLibs.interfaces.dataprovider.CandleDataProvider;
import research.sg.edu.edapp.ChartLibs.renderer.CandleStickChartRenderer;

/**
 * Financial chart type that draws candle-sticks (OHCL chart).
 *
 * @author Philipp Jahoda
 */
public class CandleStickChart extends research.sg.edu.edapp.ChartLibs.charts.BarLineChartBase<CandleData> implements CandleDataProvider {

    public CandleStickChart(Context context) {
        super(context);
    }

    public CandleStickChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CandleStickChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new CandleStickChartRenderer(this, mAnimator, mViewPortHandler);

        getXAxis().setSpaceMin(0.5f);
        getXAxis().setSpaceMax(0.5f);
    }

    @Override
    public CandleData getCandleData() {
        return mData;
    }
}
