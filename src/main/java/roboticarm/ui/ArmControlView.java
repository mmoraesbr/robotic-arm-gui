package roboticarm.ui;

import roboticarm.model.PartMoving;
import roboticarm.model.PartPosition;

import java.util.List;

/**
 * Created by mmoraes on 18/06/17.
 */
public interface ArmControlView {
    void moveToHomeView();

    void moveToRecordingView();

    void savePartMoving(PartMoving partMoving);

    void refreshPart(PartPosition partPosition);

    List<PartMoving> getSavedPartsMoving();

    void clearSavedMovements();

    void disableSaveButtons();

    void moveToConnecting();

    void setConnectedDevices(List<String> devices);
}
