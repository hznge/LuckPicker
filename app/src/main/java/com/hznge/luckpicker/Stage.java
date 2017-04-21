package com.hznge.luckpicker;

/**
 * Created by hznge on 17-4-21.
 */

public class Stage {

    private static Stage sStageInstance;

    public static final int FETCHING_STAGE = 0, OPENING_STAGE = 1, FETCHED_STAGE = 2, OPENED_STAGE = 3;

    private int currentStage = FETCHED_STAGE;

    public boolean mutex = false;

    public static Stage getInstance() {
        if (sStageInstance == null)
            sStageInstance = new Stage();
        return sStageInstance;
    }

    private Stage() {
    }

    public void entering(int _stage) {
        sStageInstance.currentStage = _stage;
        mutex = false;
    }

    public int getCurrentStage() {
        return sStageInstance.currentStage;
    }
}
