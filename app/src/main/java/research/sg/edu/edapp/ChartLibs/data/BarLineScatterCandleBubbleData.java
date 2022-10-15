
package research.sg.edu.edapp.ChartLibs.data;

import java.util.List;

import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 * 
 * @author Philipp Jahoda
 */
public abstract class BarLineScatterCandleBubbleData<T extends IBarLineScatterCandleBubbleDataSet<? extends research.sg.edu.edapp.ChartLibs.data.Entry>>
        extends ChartData<T> {
    
    public BarLineScatterCandleBubbleData() {
        super();
    }

    public BarLineScatterCandleBubbleData(T... sets) {
        super(sets);
    }

    public BarLineScatterCandleBubbleData(List<T> sets) {
        super(sets);
    }
}
