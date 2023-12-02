package com.ym.base.widget.recycleview.multi_level.vo;

import java.util.List;


public interface Tree<T extends Tree> {

    int getLevel();

    List<T> getChilds();

    boolean isExpand();

}
