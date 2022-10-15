package research.sg.edu.edapp.ChartLibs.highlight;

import research.sg.edu.edapp.ChartLibs.charts.PieChart;
import research.sg.edu.edapp.ChartLibs.data.Entry;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IPieDataSet;

/**
 * Created by philipp on 12/06/16.
 */
public class PieHighlighter extends PieRadarHighlighter<PieChart> {

    public PieHighlighter(PieChart chart) {
        super(chart);
    }

    @Override
    protected research.sg.edu.edapp.ChartLibs.highlight.Highlight getClosestHighlight(int index, float x, float y) {

        IPieDataSet set = mChart.getData().getDataSet();

        final Entry entry = set.getEntryForIndex(index);

        return new research.sg.edu.edapp.ChartLibs.highlight.Highlight(index, entry.getY(), x, y, 0, set.getAxisDependency());
    }
}
