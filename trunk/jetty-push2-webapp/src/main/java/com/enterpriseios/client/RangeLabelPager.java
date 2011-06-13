package com.enterpriseios.client;

import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/26
 * Time: 12:02:08
 * To change this template use File | Settings | File Templates.
 */
public class RangeLabelPager extends AbstractPager
{

  private final HTML label = new HTML();

  public RangeLabelPager()
  {
    initWidget(label);
  }

  @Override
  protected void onRangeOrRowCountChanged()
  {
    HasRows display = getDisplay();
    Range range = display.getVisibleRange();
    int start = range.getStart();
    int end = start + range.getLength();
    label.setText(start + " - " + end + " : " + display.getRowCount(),
        HasDirection.Direction.LTR);
  }
}

