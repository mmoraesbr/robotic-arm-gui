package roboticarm.ui;

import com.fazecast.jSerialComm.SerialPort;
import io.reactivex.Observable;
import roboticarm.model.PartMoving;
import roboticarm.model.PartPosition;
import roboticarm.model.RoboticArm;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by mmoraes on 17/06/17.
 */
public class ArmControlPresenter {
    RoboticArm roboticArm = new RoboticArm();

    ArmControlView armControlView;

    Executor executor = Executors.newSingleThreadExecutor();

    public ArmControlPresenter(ArmControlView armControlView) {
        this.armControlView = armControlView;
    }

    public void connect(String usb) {
        armControlView.moveToConnecting();
        executor.execute(() -> roboticArm.connect(usb).subscribe(o -> armControlView.moveToRecordingView()));
    }

    public void disconnect() {
        roboticArm.disconnect();
    }

    public void startNewMovements() {
        armControlView.moveToRecordingView();
    }

    public void savePositionClicked(PartPosition partPosition) {
        PartMoving partMoving = PartMoving.builder()
                .part(partPosition.getPart())
                .position(partPosition.getPosition())
                .blocking(false)
                .speed(30).build();
        armControlView.savePartMoving(partMoving);
    }

    public void decrement(PartPosition partPosition) {
        PartMoving partMoving = PartMoving.builder()
                .part(partPosition.getPart())
                .position(partPosition.getPosition() - 1)
                .speed(30).build();
        roboticArm.move(partMoving).subscribe(partPositionOptional ->  {
            if (partPositionOptional.isPresent()) {
                armControlView.refreshPart(partPositionOptional.get());
            }
        });
    }

    public void increment(PartPosition partPosition) {
        PartMoving partMoving = PartMoving.builder()
                .part(partPosition.getPart())
                .position(partPosition.getPosition() + 1)
                .speed(30).build();
        roboticArm.move(partMoving).subscribe(partPositionOptional ->  {
            if (partPositionOptional.isPresent()) {
                armControlView.refreshPart(partPositionOptional.get());
            }
        });
    }

    public Observable<Optional<PartPosition>> move(List<PartMoving> partMovingList) {
        Observable<Optional<PartPosition>> observable = roboticArm.move(partMovingList.get(0));
        for (PartMoving partMoving : partMovingList.subList(1, partMovingList.size())) {
            observable = observable.mergeWith(roboticArm.move(partMoving));
        }

        return observable;
    }

    public void executeButtonClicked() {
        List<PartMoving> partsMoving = armControlView.getSavedPartsMoving();

        roboticArm.home().doOnComplete(() -> {
            System.out.println("------------------------------------------------------------");
            Thread.sleep(1000);
            move(partsMoving).subscribe();
        }).subscribe();
    }

    public void newRecordingClicked() {
        armControlView.clearSavedMovements();
        roboticArm.home()
                .doOnComplete(() -> {
                    armControlView.disableSaveButtons();
                })
                .subscribe(o -> {
            armControlView.refreshPart(o.get());
        });
    }

    public void started() {
        List<String> devices = Stream.of(SerialPort.getCommPorts()).map(d -> d.getSystemPortName()).collect(Collectors.toList());
        armControlView.setConnectedDevices(devices);
        armControlView.moveToHomeView();
    }
}
