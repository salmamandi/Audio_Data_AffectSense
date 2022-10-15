package research.sg.edu.edapp.ChartLibs.interfaces.dataprovider;

import research.sg.edu.edapp.ChartLibs.components.YAxis.AxisDependency;
import research.sg.edu.edapp.ChartLibs.data.BarLineScatterCandleBubbleData;
import research.sg.edu.edapp.ChartLibs.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends research.sg.edu.edapp.ChartLibs.interfaces.dataprovider.ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    boolean isInverted(AxisDependency axis);
    
    float getLowestVisibleX();
    float getHighestVisibleX();

    BarLineScatterCandleBubbleData getData();
}
