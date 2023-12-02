package com.ym.base.widget.recycleview.multi_level.ex;


import com.ym.base.widget.recycleview.multi_level.vo.Tree;


public class StopMsgException extends RuntimeException {
    private Tree tree;

    public <T extends Tree> T getTree() {
        return (T) tree;
    }

    public StopMsgException setTree(Tree tree) {
        this.tree = tree;
        return this;
    }

    public StopMsgException(String message) {
        super(message);
    }


}
