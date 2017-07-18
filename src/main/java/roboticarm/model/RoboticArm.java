package roboticarm.model;

import com.fazecast.jSerialComm.SerialPort;
import io.reactivex.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.leftPad;

/**
 * Created by mmoraes on 17/06/17.
 */
public class RoboticArm {

    private SerialPort armSerialPort;

    public Observable<Optional> connect(String usb) {
        return Observable.fromCallable(() -> {
            armSerialPort = SerialPort.getCommPort(usb);
            if (!armSerialPort.openPort()) {
                throw new IllegalStateException(usb + " not opened");
            }
            try {
                Thread.sleep(2000);
                home().subscribe();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        });
    }

    public void disconnect() {
        if (armSerialPort != null) {
            armSerialPort.closePort();
        }
    }

    public Observable<Optional<PartPosition>> home() {
        PartMoving arm = PartMoving.builder()
                .part(Part.Arm).position(75).blocking(true).speed(30).build();
        PartMoving elevator = PartMoving.builder()
                .part(Part.Elevator).position(140).blocking(true).speed(30).build();
        PartMoving base = PartMoving.builder()
                .part(Part.Base).position(90).blocking(true).speed(30).build();
        PartMoving craw = PartMoving.builder()
                .part(Part.Craw).position(50).blocking(true).speed(30).build();
        return move(craw).mergeWith(move(arm)).mergeWith(move(elevator)).mergeWith(move(base));
    }


    public Observable<Optional<PartPosition>> move(PartMoving partMoving) {
        return Observable.create(subscriber -> {
            if (partMoving.isValid()) {
                String command = createCommand(partMoving);

                System.out.println(command);
                execute(command);

                PartPosition partPosition = PartPosition.builder().part(partMoving.part).position(partMoving.position).build();
                subscriber.onNext(Optional.of(partPosition));
            } else {
                subscriber.onNext(Optional.empty());
            }
            subscriber.onComplete();
        });
    }

    private void execute(String command) throws IOException, InterruptedException {
        armSerialPort.getOutputStream().write(command.getBytes());
        InputStream inputStream = armSerialPort.getInputStream();
        while (inputStream.available() == 0) {
            Thread.sleep(15);
        }
        // le o ACK para esperar a execucao do comando
        inputStream.read();
    }

    private String createCommand(PartMoving partMoving) {
        StringBuilder command = new StringBuilder();
        command.append(partMoving.getPart().getCode()).append(leftPad(partMoving.getPosition().toString(), 3, '0'));
        command.append(leftPad(partMoving.getSpeed().toString(), 3, '0')).append((partMoving.isBlocking()) ? '1' : '0');
        return command.toString();
    }
}
