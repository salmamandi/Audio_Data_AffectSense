package research.sg.edu.edapp.ChartLibs.interfaces.dataprovider;

import research.sg.edu.edapp.ChartLibs.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}
