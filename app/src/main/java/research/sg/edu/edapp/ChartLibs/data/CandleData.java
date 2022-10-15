package research.sg.edu.edapp.ChartLibs.data;

import java.util.List;

import research.sg.edu.edapp.ChartLibs.interfaces.datasets.ICandleDataSet;

public class CandleData extends BarLineScatterCandleBubbleData<ICandleDataSet> {

    public CandleData() {
        super();
    }

    public CandleData(List<ICandleDataSet> dataSets) {
        super(dataSets);
    }

    public CandleData(ICandleDataSet... dataSets) {
        super(dataSets);
    }
}
