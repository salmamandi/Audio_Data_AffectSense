package research.sg.edu.edapp.ChartLibs.interfaces.dataprovider;

import research.sg.edu.edapp.ChartLibs.data.CombinedData;

/**
 * Created by philipp on 11/06/16.
 */
public interface CombinedDataProvider extends research.sg.edu.edapp.ChartLibs.interfaces.dataprovider.LineDataProvider, BarDataProvider, BubbleDataProvider, CandleDataProvider, ScatterDataProvider {

    CombinedData getCombinedData();
}
