
package research.sg.edu.edapp.ChartLibs.data;

import java.util.ArrayList;
import java.util.List;

import research.sg.edu.edapp.ChartLibs.charts.ScatterChart;
import research.sg.edu.edapp.ChartLibs.interfaces.datasets.IScatterDataSet;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.ChevronDownShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.ChevronUpShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.CircleShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.CrossShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.IShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.SquareShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.TriangleShapeRenderer;
import research.sg.edu.edapp.ChartLibs.renderer.scatter.XShapeRenderer;
import research.sg.edu.edapp.ChartLibs.utils.ColorTemplate;

public class ScatterDataSet extends LineScatterCandleRadarDataSet<research.sg.edu.edapp.ChartLibs.data.Entry> implements IScatterDataSet {

    /**
     * the size the scattershape will have, in density pixels
     */
    private float mShapeSize = 15f;

    /**
     * Renderer responsible for rendering this DataSet, default: square
     */
    protected IShapeRenderer mShapeRenderer = new SquareShapeRenderer();

    /**
     * The radius of the hole in the shape (applies to Square, Circle and Triangle)
     * - default: 0.0
     */
    private float mScatterShapeHoleRadius = 0f;

    /**
     * Color for the hole in the shape.
     * Setting to `ColorTemplate.COLOR_NONE` will behave as transparent.
     * - default: ColorTemplate.COLOR_NONE
     */
    private int mScatterShapeHoleColor = ColorTemplate.COLOR_NONE;

    public ScatterDataSet(List<research.sg.edu.edapp.ChartLibs.data.Entry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public research.sg.edu.edapp.ChartLibs.data.DataSet<research.sg.edu.edapp.ChartLibs.data.Entry> copy() {

        List<research.sg.edu.edapp.ChartLibs.data.Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < mValues.size(); i++) {
            yVals.add(mValues.get(i).copy());
        }

        ScatterDataSet copied = new ScatterDataSet(yVals, getLabel());
        copied.mDrawValues = mDrawValues;
        copied.mValueColors = mValueColors;
        copied.mColors = mColors;
        copied.mShapeSize = mShapeSize;
        copied.mShapeRenderer = mShapeRenderer;
        copied.mScatterShapeHoleRadius = mScatterShapeHoleRadius;
        copied.mScatterShapeHoleColor = mScatterShapeHoleColor;
        copied.mHighlightLineWidth = mHighlightLineWidth;
        copied.mHighLightColor = mHighLightColor;
        copied.mHighlightDashPathEffect = mHighlightDashPathEffect;

        return copied;
    }

    /**
     * Sets the size in density pixels the drawn scattershape will have. This
     * only applies for non custom shapes.
     *
     * @param size
     */
    public void setScatterShapeSize(float size) {
        mShapeSize = size;
    }

    @Override
    public float getScatterShapeSize() {
        return mShapeSize;
    }

    /**
     * Sets the ScatterShape this DataSet should be drawn with. This will search for an available IShapeRenderer and set this
     * renderer for the DataSet.
     *
     * @param shape
     */
    public void setScatterShape(ScatterChart.ScatterShape shape) {
        mShapeRenderer = getRendererForShape(shape);
    }

    /**
     * Sets a new IShapeRenderer responsible for drawing this DataSet.
     * This can also be used to set a custom IShapeRenderer aside from the default ones.
     *
     * @param shapeRenderer
     */
    public void setShapeRenderer(IShapeRenderer shapeRenderer) {
        mShapeRenderer = shapeRenderer;
    }

    @Override
    public IShapeRenderer getShapeRenderer() {
        return mShapeRenderer;
    }

    /**
     * Sets the radius of the hole in the shape (applies to Square, Circle and Triangle)
     * Set this to <= 0 to remove holes.
     *
     * @param holeRadius
     */
    public void setScatterShapeHoleRadius(float holeRadius) {
        mScatterShapeHoleRadius = holeRadius;
    }

    @Override
    public float getScatterShapeHoleRadius() {
        return mScatterShapeHoleRadius;
    }

    /**
     * Sets the color for the hole in the shape.
     *
     * @param holeColor
     */
    public void setScatterShapeHoleColor(int holeColor) {
        mScatterShapeHoleColor = holeColor;
    }

    @Override
    public int getScatterShapeHoleColor() {
        return mScatterShapeHoleColor;
    }

    public static IShapeRenderer getRendererForShape(ScatterChart.ScatterShape shape) {

        switch (shape) {
            case SQUARE: return new SquareShapeRenderer();
            case CIRCLE: return new CircleShapeRenderer();
            case TRIANGLE: return new TriangleShapeRenderer();
            case CROSS: return new CrossShapeRenderer();
            case X: return new XShapeRenderer();
            case CHEVRON_UP: return new ChevronUpShapeRenderer();
            case CHEVRON_DOWN: return new ChevronDownShapeRenderer();
        }

        return null;
    }
}
