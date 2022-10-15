package research.sg.edu.edapp.ChartLibs.highlight;

/**
 * Created by philipp on 10/06/16.
 */
public interface IHighlighter
{

    /**
     * Returns a Highlight object corresponding to the given x- and y- touch positions in pixels.
     *
     * @param x
     * @param y
     * @return
     */
    research.sg.edu.edapp.ChartLibs.highlight.Highlight getHighlight(float x, float y);
}
