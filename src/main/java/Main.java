import roboticarm.ui.ArmControlWindow;

import java.io.IOException;

/*
 * This Java source file was auto generated by running 'gradle buildInit --type java-library'
 * by 'mmoraes' at '6/15/17 9:53 PM' with Gradle 2.7
 *
 * @author mmoraes, @date 6/15/17 9:53 PM
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ArmControlWindow armControlWindow = new ArmControlWindow();
        armControlWindow.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                armControlWindow.stop();
            }
        });
    }
}
