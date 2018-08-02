package lecho.lib.hellocharts.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.util.List;

import lecho.lib.hellocharts.BuildConfig;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.DummyColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.provider.ColumnChartDataProvider;
import lecho.lib.hellocharts.renderer.ColumnChartRenderer;

/**
 * ColumnChart/BarChart, supports subcolumns, stacked collumns and negative values.
 *
 * @author Leszek Wach
 */
public class ColumnChartView extends AbstractChartView implements ColumnChartDataProvider {
    private boolean crooped = true;
    private static final String TAG = "ColumnChartView";
    private ColumnChartData data;
    private ColumnChartOnValueSelectListener onValueTouchListener = new DummyColumnChartOnValueSelectListener();

    public ColumnChartView(Context context) {
        this(context, null, 0);
    }

    public ColumnChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColumnChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChartRenderer(new ColumnChartRenderer(context, this, this));
        setColumnChartData(ColumnChartData.generateDummyData());
    }

    @Override
    public ColumnChartData getColumnChartData() {
        return data;
    }

    @Override
    public void setColumnChartData(ColumnChartData data) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Setting data for ColumnChartView");
        }

        if (null == data) {
            this.data = ColumnChartData.generateDummyData();
        } else {
            this.data = data;
        }

        super.onChartDataChange();

        if (!crooped) { //and slove Last AxisValue in chart is cropped 解决图表中最后一个轴值是裁剪的
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    int some_additional_value = 0;
                    List<AxisValue> axisValues = getAxesRenderer().getAutoAxisYLeftValue();
                    if (axisValues.size() >= 2) {
                        some_additional_value = (int) (axisValues.get(1).getValue() - axisValues.get(0).getValue());
                    }
                    //set chart data to initialize viewport, otherwise it will be[0,0;0,0]
                    //get initialized viewport and change if ranges according to your needs.
                    final Viewport v = new Viewport(getMaximumViewport());
                    v.top = v.top + some_additional_value; //example max value
                    setMaximumViewport(v);
                    setCurrentViewport(v);
                    //Optional step: disable viewport recalculations, thanks to this animations will not change viewport automatically.
                    setViewportCalculationEnabled(false);
                }
            }, 800);

        }

    }

    @Override
    public ColumnChartData getChartData() {
        return data;
    }

    @Override
    public void callTouchListener() {
        SelectedValue selectedValue = chartRenderer.getSelectedValue();

        if (selectedValue.isSet()) {
            SubcolumnValue value = data.getColumns().get(selectedValue.getFirstIndex()).getValues()
                    .get(selectedValue.getSecondIndex());
            onValueTouchListener.onValueSelected(selectedValue.getFirstIndex(), selectedValue.getSecondIndex(), value);
        } else {
            onValueTouchListener.onValueDeselected();
        }
    }

    public ColumnChartOnValueSelectListener getOnValueTouchListener() {
        return onValueTouchListener;
    }

    public void setOnValueTouchListener(ColumnChartOnValueSelectListener touchListener) {
        if (null != touchListener) {
            this.onValueTouchListener = touchListener;
        }
    }

    public void setAxisValueCropped(boolean cropped) {
        this.crooped = cropped;
    }
}
