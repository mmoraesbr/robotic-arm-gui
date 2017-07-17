package roboticarm.ui;

import roboticarm.model.Part;
import roboticarm.model.PartMoving;
import roboticarm.model.PartPosition;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by mmoraes on 18/06/17.
 */
public class ArmControlWindow extends JFrame implements ArmControlView {

    private ArmControlPresenter armControlPresenter;
    private HomePanel homePanel;
    private ControlPanel armControlPanel;
    private PositionsSavedPanel positionsSavedPanel;

    public ArmControlWindow() throws HeadlessException {
        setBounds(10, 10, 350, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        armControlPresenter = new ArmControlPresenter(this);
        createUI();
    }

    private void createUI() {
        homePanel = new HomePanel();
        armControlPanel = new ControlPanel();
        positionsSavedPanel = new PositionsSavedPanel();
    }

    public void start() {
        armControlPresenter.started();
        setVisible(true);
    }

    public void stop() {
        armControlPresenter.disconnect();
    }

    @Override
    public void moveToConnecting() {
        SwingUtilities.invokeLater(() -> {
            getContentPane().removeAll();
            invalidate();
            JLabel statusLabel = new JLabel("Conectando...");
            add(statusLabel);
            revalidate();
            repaint();
        });
    }

    @Override
    public void moveToHomeView() {
        SwingUtilities.invokeLater(() -> {
            getContentPane().removeAll();
            invalidate();
            add(homePanel);
            revalidate();
            repaint();
        });
    }

    @Override
    public void moveToRecordingView() {
        SwingUtilities.invokeLater(() -> {
            getContentPane().removeAll();
            invalidate();
            JPanel container = new JPanel();
            container.setLayout(new GridLayout(0, 1));
            container.add(armControlPanel);
            container.add(positionsSavedPanel);
            add(container);
            revalidate();
            repaint();
        });
    }

    @Override
    public void refreshPart(PartPosition partPosition) {
        armControlPanel.refresh(partPosition);
    }

    @Override
    public void savePartMoving(PartMoving partMoving) {
        positionsSavedPanel.save(partMoving);
    }

    @Override
    public List<PartMoving> getSavedPartsMoving() {
        return this.positionsSavedPanel.getSavedPartsMoving();
    }

    @Override
    public void clearSavedMovements() {
        positionsSavedPanel.clear();
    }

    @Override
    public void disableSaveButtons() {
        armControlPanel.disableSaveButtons();
    }

    @Override
    public void setConnectedDevices(List<String> devices) {
        homePanel.setConnectedDevices(devices);
    }

    private class HomePanel extends JPanel {

        private final JComboBox devices;

        public HomePanel() {
            devices = new JComboBox();
            add(devices);
            JButton newRecording = new JButton("Connect");
            add(newRecording);
//            JButton testBtn = new JButton("Demo");
//            add(testBtn);
            newRecording.addActionListener((e) -> {
                armControlPresenter.connect("/dev/" + devices.getSelectedItem());
            });
        }

        void setConnectedDevices(List<String> connectedDevices) {
            devices.setModel(new DefaultComboBoxModel<>(connectedDevices.toArray()));
        }
    }

    private class ControlPanel extends JPanel {

        private class PartControlPanel {
            JButton saveBtn = new JButton();
            private final JLabel status;
            private PartPosition partPosition;


            private PartControlPanel(PartPosition partPosition) {
                this.partPosition = partPosition;
                this.status = new JLabel();
                refreshStatus();
            }

            private void refreshStatus() {
                SwingUtilities.invokeLater(() -> {
                    status.setText(partPosition.getPart().getName() + ":" + partPosition.getPosition());
                });
            }

            public void refreshPosition(PartPosition partPosition) {
                this.partPosition = partPosition;
                refreshStatus();
            }
        }

        private final PartControlPanel base;
        private final PartControlPanel elevator;
        private final PartControlPanel craw;
        private final PartControlPanel arm;

        public ControlPanel() {
            GridLayout controlLayout = new GridLayout(0, 3);
            controlLayout.setVgap(10);
            controlLayout.setHgap(10);
            setLayout(controlLayout);

            this.base = new PartControlPanel(PartPosition.builder()
                    .part(Part.Base).position(90).build());
            this.elevator = new PartControlPanel(PartPosition.builder()
                    .part(Part.Elevator).position(140).build());
            this.craw = new PartControlPanel(PartPosition.builder()
                    .part(Part.Craw).position(50).build());
            this.arm = new PartControlPanel(PartPosition.builder()
                    .part(Part.Arm).position(70).build());

            add(base);
            add(craw);
            add(arm);
            add(elevator);

        }

        private void add(PartControlPanel partPanel) {
            JButton saveBtn = partPanel.saveBtn;
            saveBtn.setEnabled(false);
            saveBtn.setIcon(new ImageIcon("src/main/resources/save.png"));
            saveBtn.addActionListener(e -> {
                armControlPresenter.savePositionClicked(partPanel.partPosition);
                saveBtn.setEnabled(false);
            });

            JButton minusButton = new JButton("-");
            minusButton.addMouseListener(new PartUpdateListener(() -> {
                saveBtn.setEnabled(true);
                armControlPresenter.decrement(partPanel.partPosition);
            }));
            JButton plusButton = new JButton("+");
            plusButton.addMouseListener(new PartUpdateListener(() -> {
                saveBtn.setEnabled(true);
                armControlPresenter.increment(partPanel.partPosition);
            }));

            add(minusButton);

            JPanel panel = new JPanel();
            panel.add(partPanel.status);

            panel.add(saveBtn);

            add(panel);

            add(plusButton);
        }

        public void disableSaveButtons() {
            base.saveBtn.setEnabled(false);
            arm.saveBtn.setEnabled(false);
            craw.saveBtn.setEnabled(false);
            elevator.saveBtn.setEnabled(false);
        }

        public void refresh(PartPosition partPosition) {
            PartControlPanel partControlPanel = null;
            switch (partPosition.getPart()) {
                case Arm:
                    partControlPanel = arm;
                    break;
                case Craw:
                    partControlPanel = craw;
                    break;
                case Elevator:
                    partControlPanel = elevator;
                    break;
                case Base:
                    partControlPanel = base;
                    break;
            }
            partControlPanel.refreshPosition(partPosition);
        }



        private class PartUpdateListener extends MouseAdapter {
            private final Runnable action;
            private ScheduledFuture<?> scheduledFuture;

            public PartUpdateListener(Runnable action) {
                this.action = action;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                action.run();
                scheduledFuture = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(action,
                        300, 30, TimeUnit.MILLISECONDS);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                scheduledFuture.cancel(false);
            }

        }

    }

    private class PositionsSavedPanel extends JPanel {
        private JTable commandsTable;
        private DefaultTableModel commandsTableModel;

        public PositionsSavedPanel() {
            setLayout(new BorderLayout());
            commandsTableModel = createTableModel();
            commandsTable = new JTable(commandsTableModel);
            commandsTable.setFillsViewportHeight(true);
            add(new JScrollPane(commandsTable), BorderLayout.CENTER);
            //        add(commandsTable, BorderLayout.CENTER);
            JButton executeButton = new JButton("Executar");
            executeButton.addActionListener(e -> {
                armControlPresenter.executeButtonClicked();
            });
            add(executeButton, BorderLayout.NORTH);
            JButton newRecording = new JButton("Novo");
            newRecording.addActionListener(e -> {
                armControlPresenter.newRecordingClicked();
            });
            add(newRecording, BorderLayout.SOUTH);
        }

        private DefaultTableModel createTableModel() {
            commandsTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0;
                }
            };
            commandsTableModel.addColumn("Parte");
            commandsTableModel.addColumn("Angulo");
            commandsTableModel.addColumn("Velocidade");
            return commandsTableModel;
        }

        public void save(PartMoving partMoving) {
            commandsTableModel.addRow(new Object[]{partMoving.getPart(), partMoving.getPosition(), partMoving.getSpeed()});
        }

        public List<PartMoving> getSavedPartsMoving() {
            List<PartMoving> partMovingList = new LinkedList<>();
            for (int i = 0; i < commandsTableModel.getRowCount(); i++) {
                Part part = (Part) commandsTableModel.getValueAt(i, 0);
                Integer position = new Integer(commandsTableModel.getValueAt(i, 1).toString());
                Integer speed = new Integer(commandsTableModel.getValueAt(i, 2).toString());
                PartMoving partMoving = PartMoving.builder()
                        .part(part)
                        .position(position)
                        .speed(speed)
                        .blocking(true)
                        .build();
                partMovingList.add(partMoving);
            }
            return partMovingList;
        }

        public void clear() {
            commandsTable.setModel(createTableModel());
        }
    }

}
