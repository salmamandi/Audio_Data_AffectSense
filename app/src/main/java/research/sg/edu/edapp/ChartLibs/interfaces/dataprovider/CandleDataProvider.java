package research.sg.edu.edapp.ChartLibs.interfaces.dataprovider;

import research.sg.edu.edapp.ChartLibs.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}
