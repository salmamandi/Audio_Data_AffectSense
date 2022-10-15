package research.sg.edu.edapp.ChartLibs.interfaces.dataprovider;

import research.sg.edu.edapp.ChartLibs.components.YAxis;
import research.sg.edu.edapp.ChartLibs.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
