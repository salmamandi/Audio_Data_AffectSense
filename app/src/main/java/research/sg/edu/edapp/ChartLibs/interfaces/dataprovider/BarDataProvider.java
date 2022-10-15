package research.sg.edu.edapp.ChartLibs.interfaces.dataprovider;

import research.sg.edu.edapp.ChartLibs.data.BarData;

public interface BarDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BarData getBarData();
    boolean isDrawBarShadowEnabled();
    boolean isDrawValueAboveBarEnabled();
    boolean isHighlightFullBarEnabled();
}
