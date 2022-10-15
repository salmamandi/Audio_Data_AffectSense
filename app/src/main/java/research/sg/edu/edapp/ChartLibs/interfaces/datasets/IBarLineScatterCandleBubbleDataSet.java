package research.sg.edu.edapp.ChartLibs.interfaces.datasets;

import research.sg.edu.edapp.ChartLibs.data.Entry;

/**
 * Created by philipp on 21/10/15.
 */
public interface IBarLineScatterCandleBubbleDataSet<T extends Entry> extends research.sg.edu.edapp.ChartLibs.interfaces.datasets.IDataSet<T> {

    /**
     * Returns the color that is used for drawing the highlight indicators.
     *
     * @return
     */
    int getHighLightColor();
}
